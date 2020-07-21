/*
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
package org.apache.mybatis.dbperms.parser;

import java.util.Optional;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;

public interface ITablePermissionScriptHandler {

	/*
	 *  SQL 处理
	 *
	 * @param metaHandler 元对象
	 * @param originalSQL 当前执行 SQL
	 * @return
	 */
	default Optional<String> process(MetaStatementHandler metaHandler, String segmentSQL) {
		String permissionedSQL = dynamicPermissionedSQL(metaHandler, segmentSQL);
		return Optional.ofNullable(permissionedSQL);
	}

	/*
	 * 生成动态SQL，无改变返回 NULL
	 *
	 * @param metaHandler 元对象
	 * @param originalSQL 当前执行 SQL
	 * @return String
	 */
	String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String segmentSQL);

}
