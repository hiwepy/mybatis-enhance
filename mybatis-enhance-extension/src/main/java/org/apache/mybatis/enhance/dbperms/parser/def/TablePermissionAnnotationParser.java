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
package org.apache.mybatis.enhance.dbperms.parser.def;

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectAnnotationPermissionParser;
import net.sf.jsqlparser.util.SelectAnnotationPermissionsParser;
import net.sf.jsqlparser.util.SelectAnnotationSpecialPermissionParser;
import net.sf.jsqlparser.util.SelectAnnotationSpecialPermissionsParser;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.exception.MybatisException;
import org.apache.mybatis.enhance.annotation.RequiresPermission;
import org.apache.mybatis.enhance.annotation.RequiresSpecialPermission;
import org.apache.mybatis.enhance.dbperms.parser.ITablePermissionParser;

@Data
@Accessors(chain = true)
public class TablePermissionAnnotationParser implements ITablePermissionParser {

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

    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermission[] permissions) {
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
            	select.accept(new SelectAnnotationPermissionsParser(metaHandler, permissions));
            	// 获取处理后的SQL
            	parsedSQL = select.getSelectBody().toString();
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
        return parsedSQL;
    }

    public String parser(MetaStatementHandler metaHandler, String sql, RequiresPermission permission) {
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
            	select.accept(new SelectAnnotationPermissionParser(metaHandler, permission));
            	// 获取处理后的SQL
            	parsedSQL = select.getSelectBody().toString();
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
        return parsedSQL;
    }

	public String parser(MetaStatementHandler metaHandler, String sql, RequiresSpecialPermission[] permissions) {
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
            	select.accept(new SelectAnnotationSpecialPermissionsParser(metaHandler, permissions));
            	// 获取处理后的SQL
            	parsedSQL = select.getSelectBody().toString();
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
        return parsedSQL;
	}

	public String parser(MetaStatementHandler metaHandler, String sql, RequiresSpecialPermission permission) {
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
            	select.accept(new SelectAnnotationSpecialPermissionParser(metaHandler, permission));
            	// 获取处理后的SQL
            	parsedSQL = select.getSelectBody().toString();
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
        return parsedSQL;
	}

}
