/**
 * Copyright (c) 2018 (https://github.com/hiwepy).
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
package org.apache.mybatis.enhance.annotation;

import java.lang.annotation.*;

/**
 * 该注解用于方法，字段；指明字段
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RequiresPermissionForeign {

	/**
	 * 受限表字段关联表之间的关联条件
	 */
	public abstract ForeignCondition condition();
	/**
	 *外关联表名称（实体表名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
	 */
	public abstract String table() default "";
	/**
	 *外关联表字段（实体表字段列名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
	 */
	public abstract String column() default "";

}
