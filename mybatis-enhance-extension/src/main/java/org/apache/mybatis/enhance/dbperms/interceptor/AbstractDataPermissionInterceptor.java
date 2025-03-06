/***
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
package org.apache.mybatis.enhance.dbperms.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.AbstractInterceptorAdapter;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.mybatis.enhance.annotation.NotRequiresPermission;
import org.apache.mybatis.enhance.annotation.RequiresPermission;
import org.apache.mybatis.enhance.annotation.RequiresPermissions;
import org.apache.mybatis.enhance.annotation.RequiresSpecialPermission;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public abstract class AbstractDataPermissionInterceptor extends AbstractInterceptorAdapter {

	@Override
	protected boolean isRequireIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
		// 通过反射获取到当前MappedStatement
		MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
		// 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
		// BoundSql boundSql = metaStatementHandler.getBoundSql();
		// Object paramObject = boundSql.getParameterObject();
		// 提取被数据权限注解标记的方法
		Method method = metaStatementHandler.getMethod();
		// 获取接口类型
		Class<?> mapperInterface = metaStatementHandler.getMapperInterface();
		// 无需数据权限控制
		if(Objects.nonNull(mapperInterface) && AnnotationUtils.findAnnotation(mapperInterface, NotRequiresPermission.class) != null) {
			return false;
		}
		if( Objects.nonNull(method) &&  AnnotationUtils.findAnnotation(method, NotRequiresPermission.class) != null) {
			return false;
		}
		// 需要数据权限控制
		if (SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
			if (Objects.nonNull(mapperInterface) && AnnotationUtils.findAnnotation(mapperInterface, RequiresPermissions.class) != null) {
				return true;
			}
			if (Objects.nonNull(method) && (AnnotationUtils.findAnnotation(method, RequiresPermissions.class) != null
					|| AnnotationUtils.findAnnotation(method, RequiresPermission.class) != null
					|| AnnotationUtils.findAnnotation(method, RequiresSpecialPermission.class) != null)) {
				return true;
			}
		}
		//BeanMethodDefinitionFactory.getMethodDefinition(mappedStatement.getId(), paramObject != null ? new Class<?>[] {paramObject.getClass()} : null);
		return false;
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
