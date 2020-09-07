/*
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
package org.apache.mybatis.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataPermissionPart {

	/**
	 * 1：匹配字段过滤时候查询的表（实体表名称）
	 */
	private String table;
	/**
	 * 2：匹配字段过滤时候查询的表的关联表
	 */
	private String related;
	/**
	 * 3：匹配的过滤字段
	 */
	private String match;
	/**
	 * 4：匹配字段过滤时候查询的表的条件字段名称
	 */
	private String table_field = "tableField";
	/**
	 * 5：数据范围对象的字段名称
	 */
	private String filter;
	/**
	 * 6：原查询SQL中与对应过滤查询表的过滤字段的字段名称[此参数在 in 的模式下可没有]
	 */
	private String mapper;
	

}
