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
package org.apache.mybatis.dbperms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.parser.ITablePermissionAutowireHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionParser;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.QueryTablesNamesFinder;

@Data
@Accessors(chain = true)
public class TablePermissionAutowireParser implements ITablePermissionParser {
	
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
        //Collection<String> tables = new TableNameParser(sql).tables();
    	Collection<String> tables = new ArrayList<>();
        // 尝试另外一种方式
    	try {
    		//TablesNamesFinder tablesNamesFinder = new TablesNamesFinder(); 
    		QueryTablesNamesFinder tablesNamesFinder = new QueryTablesNamesFinder(); 
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
        String parsedSQL = sql;
        if (CollectionUtils.isNotEmpty(tables)) {
        	List<Map<String, String>> parsedList = new ArrayList<Map<String,String>>();
        	List<String> parsedTables = new ArrayList<String>();
        	// 表名统一转成小写、去重后的表名
        	List<String> distinctTables = tables.stream().map(table -> StringUtils.lowerCase(table)).distinct().collect(Collectors.toList());
            for (final String tableName : distinctTables) {
            	// 判断表格是否已经处理过
            	if(parsedTables.contains(tableName)) {
            		continue;
            	}
            	// 有处理器
                if (null != tablePermissionHandler) {
                	// 处理后的SQL	
                	Optional<String> permissionedSQL = tablePermissionHandler.process(metaHandler, tableName);
                	if (null != permissionedSQL && permissionedSQL.isPresent()) {
                		Map<String, String> parsedMap = new HashMap<String, String>();
                		parsedMap.put("table", tableName);
                		parsedMap.put("sql", permissionedSQL.get());
                		parsedList.add(parsedMap);
                		parsedTables.add(tableName);
                    }
                }
            }
            // 循环替换已经解析后的SQL
            for (Map<String, String> parsedMap : parsedList) {
            	
            	// 查找表名
            	Pattern pattern_find = Pattern.compile("(?:" + parsedMap.get("table") + ")+", Pattern.CASE_INSENSITIVE);
             	// 匹配所有匹配的表名
         		Matcher matcher = pattern_find.matcher(parsedSQL);
         		// 查找匹配的片段
     			while (matcher.find()) {
     				
     				// 获取匹配的内容
     				String full_segment = matcher.group(0);
     				// 取得{}内容开始结束位置
     				int begain = parsedSQL.indexOf(full_segment);
     				int end = begain + full_segment.length();
     				//	将原来SQL表替换为处理后的SQL 
                	parsedSQL = parsedSQL.substring(0, begain) + parsedMap.get("sql") + parsedSQL.substring(end);
     			}
			}
            
		}
        return parsedSQL;
    }
	
}
