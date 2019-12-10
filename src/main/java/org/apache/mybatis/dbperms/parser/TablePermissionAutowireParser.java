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

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Data
@Accessors(chain = true)
public class TablePermissionAutowireParser implements ITablePermissionParser {
	
	private TablesNamesFinder tablesNamesFinder = new TablesNamesFinder(); 
	private ITablePermissionAutowireHandler tablePermissionHandler;

	private volatile boolean initialized = false;

    /**
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

    /**
     * Internal initialization of the object.
     */
    protected void internalInit() {};
    
    public String parser(MetaStatementHandler metaHandler, String sql) {
    	if (!this.doFilter(metaHandler, sql)) {
    		 return sql;
		}
    	this.init();
        Collection<String> tables = new TableNameParser(sql).tables();
        // 尝试另外一种方式
        if (CollectionUtils.isEmpty(tables)) {
        	try {
				Statements statements = CCJSqlParserUtil.parseStatements(sql);
				for (Statement statement : statements.getStatements()) {
					if (null != statement && statement instanceof Select) { 
					   Select selectStatement = (Select) statement; 
					   List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
					   for (String tableName : tableList) {
						   tables.add(tableName);
					   }
				    }
				}
			} catch (JSQLParserException e) {
			}
        }
        String parsedSql = sql;
        if (CollectionUtils.isNotEmpty(tables)) {
            for (final String table : tables) {
                if (null != tablePermissionHandler && tablePermissionHandler.match(metaHandler, table)) {
                	parsedSql = tablePermissionHandler.process(metaHandler, parsedSql, table);
                }
            }
		}
        return parsedSql;
    }
	
}
