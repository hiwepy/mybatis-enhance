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
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.Assert;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.ibatis.utils.MapUtils;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class TablePermissionAnnotationParser implements ITablePermissionParser {

	private TablesNamesFinder tablesNamesFinder = new TablesNamesFinder(); 
	private Map<String, ITablePermissionAnnotationHandler> tablePermissionHandlerMap;
	
    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermissions permissions) {
    	if (!this.doFilter(metaHandler, sql)) {
   		 return sql;
		}
    	Assert.isTrue(MapUtils.isNotEmpty(tablePermissionHandlerMap), "tablePermissionHandlerMap is empty.");
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
        RequiresPermission[] permissionArr  = permissions.value();
        if (CollectionUtils.isNotEmpty(tables) && ArrayUtils.isNotEmpty(permissionArr)) {
			for (RequiresPermission permission : permissionArr) {
				String tableName = StringUtils.lowerCase(permission.table());
				if(tables.stream().anyMatch(table -> StringUtils.equalsAnyIgnoreCase(table, tableName))) {
					ITablePermissionAnnotationHandler tableNameHandler = tablePermissionHandlerMap.get(tableName);
	                if (null != tableNameHandler) {
	                	parsedSql = tableNameHandler.process(metaHandler, parsedSql, permission);
	                }
				}
			}
		}
        return parsedSql;
    }
    
    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermission permission) {
    	Assert.isTrue(MapUtils.isNotEmpty(tablePermissionHandlerMap), "tablePermissionHandlerMap is empty.");
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
        if (CollectionUtils.isNotEmpty(tables) && null != permission) {
        	String tableName = StringUtils.lowerCase(permission.table());
			if(tables.stream().anyMatch(table -> StringUtils.equalsAnyIgnoreCase(table, tableName))) {
				ITablePermissionAnnotationHandler tableNameHandler = tablePermissionHandlerMap.get(tableName);
                if (null != tableNameHandler) {
                	parsedSql = tableNameHandler.process(metaHandler, parsedSql, permission);
                }
			}
		}
        return parsedSql;
    }
    
    public ITablePermissionAnnotationHandler getTablePermissionHandler(String tableName) {
    	return tablePermissionHandlerMap.get(tableName);
	}
    
}
