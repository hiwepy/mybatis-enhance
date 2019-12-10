/**
 * Copyright (c) 2018, vindell (https://github.com/vindell).
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
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.AbstractInterceptorAdapter;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;
 
public abstract class AbstractDataPermissionInterceptor extends AbstractInterceptorAdapter {

	protected static Logger LOG = LoggerFactory.getLogger(AbstractDataPermissionInterceptor.class);
	
	@Override
	protected boolean isRequireIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
		// 通过反射获取到当前MappedStatement
		MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
		// 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
		// BoundSql boundSql = metaStatementHandler.getBoundSql();
		// Object paramObject = boundSql.getParameterObject();
		//提取被国际化注解标记的方法
		Method method = metaStatementHandler.getMethod(); 
		// 获取接口类型
		Class<?> mapperInterface = metaStatementHandler.getMapperInterface();
		
		//BeanMethodDefinitionFactory.getMethodDefinition(mappedStatement.getId(), paramObject != null ? new Class<?>[] {paramObject.getClass()} : null);
		return  SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) && method != null &&
				(AnnotationUtils.findAnnotation(mapperInterface, RequiresPermissions.class) != null || 
				 AnnotationUtils.findAnnotation(method, RequiresPermissions.class) != null || AnnotationUtils.findAnnotation(method, RequiresPermission.class) != null);
	}
	
	protected boolean isIntercepted(CacheKey cacheKey) {
		//获取当前线程绑定的上下文对象
		String uniqueKey = DigestUtils.md5DigestAsHex(cacheKey.toString().getBytes());
		if(! extraContext.containsKey(uniqueKey)){
			return true;
		}
		extraContext.put(uniqueKey, cacheKey);
		return false;
	}
	
	@Override
	public void doDestroyIntercept(Invocation invocation) throws Throwable {
		extraContext.clear();
	}
	
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);  
	}

	@Override
	public void setInterceptProperties(Properties properties) {
		
	}
	
}
