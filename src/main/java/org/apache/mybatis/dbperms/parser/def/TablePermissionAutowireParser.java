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
package org.apache.mybatis.dbperms.parser.def;

import org.apache.ibatis.exception.MybatisException;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionAutowireHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionParser;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectAutowirePermissionParser;

@Data
@Accessors(chain = true)
public class TablePermissionAutowireParser implements ITablePermissionParser {
	
	private ITablePermissionAutowireHandler tablePermissionHandler;

	private volatile boolean initialized = false;

    /*
     * Initialize the object.
     */
    public void init() {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    internalInit();
                    this.initialized = true;
                }
            }
        }
    }

    /*
     * Internal initialization of the object.
     */
    protected void internalInit() {};
    
    public String parser(MetaStatementHandler metaHandler, String sql) {
    	if (!this.doFilter(metaHandler, sql)) {
    		 return sql;
		}
    	this.init();
    	String parsedSQL = sql;
    	try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (null != statement && statement instanceof Select) { 
            	Select select = (Select) statement;
            	// 动态修改SQL
            	select.accept(new SelectAutowirePermissionParser(this.getTablePermissionHandler(), metaHandler));
            	// 获取处理后的SQL
            	parsedSQL = select.getSelectBody().toString();
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
        return parsedSQL;
    }
	
}
