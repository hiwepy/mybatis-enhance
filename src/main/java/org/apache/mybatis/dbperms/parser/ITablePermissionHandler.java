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
package org.apache.mybatis.dbperms.parser;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;

public interface ITablePermissionHandler {

	 /**
     * 表名 SQL 处理
     *
     * @param metaObject 元对象
     * @param sql        当前执行 SQL
     * @param tableName  表名
     * @return
     */
    default String process(MetaStatementHandler metaObject, String sql, String tableName) {
        String dynamicTableName = dynamicPermissionedSQL(metaObject, sql, tableName);
        if (null != dynamicTableName && !dynamicTableName.equalsIgnoreCase(tableName)) {
            return sql.replaceAll(tableName, dynamicTableName);
        }
        return sql;
    }

    /**
     * 生成动态表名，无改变返回 NULL
     *
     * @param metaObject 元对象
     * @param sql        当前执行 SQL
     * @param tableName  表名
     * @return String
     */
    String dynamicPermissionedSQL(MetaStatementHandler metaObject, String sql, String tableName);
    
}
