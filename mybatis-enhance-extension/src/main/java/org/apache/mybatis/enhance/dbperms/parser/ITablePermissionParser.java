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
package org.apache.mybatis.enhance.dbperms.parser;

import org.apache.ibatis.binding.MetaStatementHandler;

public interface ITablePermissionParser {

	/**
     * <p>
     * 是否执行 SQL 解析 parser 方法
     * </p>
     *
     * @param metaObject 元对象
     * @param sql        SQL 语句
     * @return SQL 信息
     */
    default boolean doFilter(final MetaStatementHandler metaObject, final String sql) {
        // 默认 true 执行 SQL 解析, 可重写实现控制逻辑
        return true;
    }

}
