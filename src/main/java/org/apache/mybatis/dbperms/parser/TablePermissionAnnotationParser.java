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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.QueryTablesNamesFinder;

@Data
@Accessors(chain = true)
public class TablePermissionAnnotationParser implements ITablePermissionParser {

	private QueryTablesNamesFinder tablesNamesFinder = new QueryTablesNamesFinder(); 
	private ITablePermissionAnnotationHandler tablePermissionHandler = new DefaultTablePermissionAnnotationHandler();
	
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
	
	
    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermissions permissions) {
    	if (!this.doFilter(metaHandler, sql)) {
   		 return sql;
		}
    	this.init();
    	//Collection<String> tables = new TableNameParser(sql).tables();
    	Collection<String> tables = new ArrayList<>();
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
					   /*
					   selectStatement.accept(new StatementVisitorAdapter() {
						   
						   
						   
					   });*/
				    }
				}
			} catch (JSQLParserException e) {
			}
        }
        String parsedSQL = sql;
        RequiresPermission[] permissionArr  = permissions.value();
        if (CollectionUtils.isNotEmpty(tables) && ArrayUtils.isNotEmpty(permissionArr)) {
        	
        	List<Map<String, String>> parsedList = new ArrayList<Map<String,String>>();
        	List<String> parsedTables = new ArrayList<String>();
        	// 表名统一转成小写、去重后的表名
        	List<String> distinctTables = tables.stream().map(table -> StringUtils.lowerCase(table)).distinct().collect(Collectors.toList());
			// 按表名分组
        	Map<String, List<RequiresPermission>> groupingMap = Stream.of(permissionArr).collect(Collectors.groupingBy(RequiresPermission::table));
        	
        	for (String table : groupingMap.keySet()) {
        		// 表名统一转成小写
				String tableName = StringUtils.lowerCase(table);
            	// 判断表格是否已经处理过
            	if(parsedTables.contains(tableName)) {
            		continue;
            	}
            	// 有处理器且有匹配的权限控制
				if(null != tablePermissionHandler && tablePermissionHandler.match(metaHandler, tableName) && distinctTables.contains(tableName)) {
					// 处理后的SQL	
                	Optional<String> permissionedSQL = tablePermissionHandler.process(metaHandler, parsedSQL, groupingMap.get(table));
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
    
    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermission permission) {
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
        String parsedSQL = sql;
        if (CollectionUtils.isNotEmpty(tables) && null != permission) {
        	// 表名统一转成小写、去重后的表名
        	List<String> distinctTables = tables.stream().map(table -> StringUtils.lowerCase(table)).distinct().collect(Collectors.toList());
        	String tableName = StringUtils.lowerCase(permission.table());
        	// 有处理器且有匹配的权限控制
			if(null != tablePermissionHandler && tablePermissionHandler.match(metaHandler, tableName)
					&& distinctTables.contains(tableName)) {
				// 处理后的SQL	
            	Optional<String> permissionedSQL = tablePermissionHandler.process(metaHandler, parsedSQL, permission);
            	if (null != permissionedSQL && permissionedSQL.isPresent()) {
            		
            		// 查找表名
                	Pattern pattern_find = Pattern.compile("(?:" + tableName + ")+", Pattern.CASE_INSENSITIVE);
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
                    	parsedSQL = parsedSQL.substring(0, begain) + permissionedSQL.get() + parsedSQL.substring(end);
                    	
         			}
         			
                }
			}
		}
        return parsedSQL;
    }
    
}
