/*
 * Copyright (c) 2018 (https://github.com/vindell).
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
package org.apache.mybatis.dbperms.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于方法，字段；指明字段
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequiresPermission {

	/**
	 *受限表名称（实体表名称）
	 */
	public abstract String table();
	/**
	 *受限表转换后的SQL(直接使用SQL进行替换，减少性能消耗)
	 */
	public abstract String sql() default "";
	/**
	 * 数据权限项数组
	 */
	public abstract RequiresPermissionColumn[] value();
	/**
	 * 数据权限项关系 and/or
	 */
	public abstract Relational relation();
	
}
