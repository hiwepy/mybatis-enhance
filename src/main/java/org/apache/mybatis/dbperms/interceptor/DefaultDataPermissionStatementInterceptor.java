/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.mybatis.dbperms.interceptor;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;
import org.apache.mybatis.dbperms.annotation.RequiresSpecialPermission;
import org.apache.mybatis.dbperms.parser.def.TablePermissionAnnotationParser;
import org.apache.mybatis.dbperms.parser.def.TablePermissionAutowireParser;
import org.apache.mybatis.dbperms.parser.def.TablePermissionScriptParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

@Intercepts({
	@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DefaultDataPermissionStatementInterceptor extends AbstractDataPermissionInterceptor {

	protected static Logger LOG = LoggerFactory.getLogger(DefaultDataPermissionStatementInterceptor.class);
	protected final Pattern scriptPattern = Pattern.compile("(?:(?:\\{)(?:[^\\{\\}]*?)(?:\\}))+");
	protected final TablePermissionAutowireParser autowirePermissionParser;
	protected final TablePermissionAnnotationParser annotationPermissionParser;
	protected TablePermissionScriptParser scriptPermissionParser;
	
	public DefaultDataPermissionStatementInterceptor(TablePermissionAutowireParser autowirePermissionParser,
			TablePermissionAnnotationParser annotationPermissionParser) {
		this.autowirePermissionParser = autowirePermissionParser;
		this.annotationPermissionParser = annotationPermissionParser;
	}
	
	public DefaultDataPermissionStatementInterceptor(TablePermissionAutowireParser autowirePermissionParser,
			TablePermissionAnnotationParser annotationPermissionParser, 
			TablePermissionScriptParser scriptPermissionParser) {
		this.autowirePermissionParser = autowirePermissionParser;
		this.annotationPermissionParser = annotationPermissionParser;
		this.scriptPermissionParser = scriptPermissionParser;
	}
	
	@Override
	public Object doStatementIntercept(Invocation invocation, StatementHandler statementHandler,MetaStatementHandler metaStatementHandler) throws Throwable {
		
		//检查是否需要进行拦截处理
		if (isRequireIntercept(invocation, statementHandler, metaStatementHandler)) {
			// 利用反射获取到FastResultSetHandler的mappedStatement属性，从而获取到MappedStatement；
			//MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
						
			// 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
			BoundSql boundSql = metaStatementHandler.getBoundSql();
			MetaObject metaBoundSql = SystemMetaObject.forObject(boundSql);
			// 原始SQL
			String originalSQL = (String) metaBoundSql.getValue("sql");
			
			// 匹配SQL中的数据权限规则函数
			Matcher matcher = scriptPattern.matcher(originalSQL);
			if (null != scriptPermissionParser && matcher.find()) {
				// 对原始SQL进行数据范围限制条件的处理
				originalSQL = scriptPermissionParser.parser(metaStatementHandler, originalSQL);
            	//将处理后的SQL重新写入作为执行SQL
	            metaBoundSql.setValue("sql", originalSQL);
				if (LOG.isDebugEnabled()) {
					LOG.debug(" Permissioned SQL : "+ statementHandler.getBoundSql().getSql());
				}
			}
			
			// 提取被数据权限注解标记的方法
			Method method = metaStatementHandler.getMethod(); 
			// Method method = BeanMethodDefinitionFactory.getMethodDefinition(mappedStatement.getId());
			if(null != method) {
				// 获取 @RequiresPermissions 注解标记
				RequiresPermissions permissions = AnnotationUtils.findAnnotation(method, RequiresPermissions.class);
				// 需要权限控制
				if(permissions != null) {
					// 框架自动进行数据权限注入
					if(permissions.autowire()) {
						originalSQL = autowirePermissionParser.parser(metaStatementHandler, originalSQL);
					}
					// 普通字符关联权限
					else if(ArrayUtils.isNotEmpty(permissions.value())){
						originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.value());
					}
					// 特殊表关联权限
					else if(ArrayUtils.isNotEmpty(permissions.special())){
						originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.special());
					}
					// 将处理后的物理分页sql重新写入作为执行SQL
					metaBoundSql.setValue("sql", originalSQL);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Permissioned SQL : "+ statementHandler.getBoundSql().getSql());
					}
					// 将执行权交给下一个拦截器  
					return invocation.proceed();
				} 
				
				// 获取 @RequiresPermission 注解标记
				RequiresPermission permission = AnnotationUtils.findAnnotation(method, RequiresPermission.class);
				if (permission != null) {
					originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permission);
					// 将处理后的物理分页sql重新写入作为执行SQL
					metaBoundSql.setValue("sql", originalSQL);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Permissioned SQL : "+ statementHandler.getBoundSql().getSql());
					}
					// 将执行权交给下一个拦截器  
					return invocation.proceed();
				}

				// 获取 @RequiresSpecialPermission 注解标记
				RequiresSpecialPermission specialPermission = AnnotationUtils.findAnnotation(method, RequiresSpecialPermission.class);
				if (specialPermission != null) {
					originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, specialPermission);
					// 将处理后的物理分页sql重新写入作为执行SQL
					metaBoundSql.setValue("sql", originalSQL);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Permissioned SQL : "+ statementHandler.getBoundSql().getSql());
					}
					// 将执行权交给下一个拦截器  
					return invocation.proceed();
				}
			}			
			// 获取接口类型
			Class<?> mapperInterface = metaStatementHandler.getMapperInterface();
			if(null != mapperInterface) {
				RequiresPermissions	permissions = AnnotationUtils.findAnnotation(mapperInterface, RequiresPermissions.class);
				// 需要权限控制
				if(permissions != null) {
					// 框架自动进行数据权限注入
					if(permissions.autowire()) {
						originalSQL = autowirePermissionParser.parser(metaStatementHandler, originalSQL);
					}
					// 普通字符关联权限
					else if(ArrayUtils.isNotEmpty(permissions.value())){
						originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.value());
					}
					// 特殊表关联权限
					else if(ArrayUtils.isNotEmpty(permissions.special())){
						originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.special());
					}
					// 将处理后的物理分页sql重新写入作为执行SQL
					metaBoundSql.setValue("sql", originalSQL);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Permissioned SQL : "+ statementHandler.getBoundSql().getSql());
					}
					// 将执行权交给下一个拦截器  
					return invocation.proceed();
				}
			}			 
		}
		// 将执行权交给下一个拦截器  
		return invocation.proceed();
	}
	
	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {  
            return Plugin.wrap(target, this);  
        } else {  
            return target;  
        }
	}
	
}
