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
package org.apache.mybatis.enhance.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * jsqlparser解析SQL工具类
 * PlainSelect类不支持union、union all等请使用SetOperationList接口
 *
 */
public class SqlParserTool {

    static TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    /**
     * 由于jsqlparser没有获取SQL类型的原始工具，并且在下面操作时需要知道SQL类型，所以编写此工具方法
     * @param sql sql语句
     * @return sql类型，
     * @throws JSQLParserException
     */
    public static SqlType getSqlType(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        if (sqlStmt instanceof Alter) {
            return SqlType.ALTER;
        } else if (sqlStmt instanceof CreateIndex) {
            return SqlType.CREATEINDEX;
        } else if (sqlStmt instanceof CreateTable) {
            return SqlType.CREATETABLE;
        } else if (sqlStmt instanceof CreateView) {
            return SqlType.CREATEVIEW;
        } else if (sqlStmt instanceof Delete) {
            return SqlType.DELETE;
        } else if (sqlStmt instanceof Drop) {
            return SqlType.DROP;
        } else if (sqlStmt instanceof Execute) {
            return SqlType.EXECUTE;
        } else if (sqlStmt instanceof Insert) {
            return SqlType.INSERT;
        } else if (sqlStmt instanceof Merge) {
            return SqlType.MERGE;
        } else if (sqlStmt instanceof Replace) {
            return SqlType.REPLACE;
        } else if (sqlStmt instanceof Select) {
            return SqlType.SELECT;
        } else if (sqlStmt instanceof Truncate) {
            return SqlType.TRUNCATE;
        } else if (sqlStmt instanceof Update) {
            return SqlType.UPDATE;
        } else if (sqlStmt instanceof Upsert) {
            return SqlType.UPSERT;
        } else {
            return SqlType.NONE;
        }
    }

    /**
     * 获取sql操作接口,与上面类型判断结合使用
     * example:
     * String sql = "create table a(a string)";
     * SqlType sqlType = SqlParserTool.getSqlType(sql);
     * if(sqlType.equals(SqlType.SELECT)){
     *     Select statement = (Select) SqlParserTool.getStatement(sql);
     *  }
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static Statement getStatement(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        return sqlStmt;
    }

    /**
     * 获取tables的表名
     * @param statement
     * @return
     */
    public static Set<String> getTables(Statement statement) throws JSQLParserException {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTables(statement);
    }

    /**
     * 获取tables的表名
     * @param sql
     * @return
     */
    public static Set<String> getTables(String sql) throws JSQLParserException {
        Set<String> tableList = TablesNamesFinder.findTables(sql);
        return tableList;
    }

    /**
     * 获取join层级
     * @param selectBody
     * @return
     */
    public static List<Join> getJoins(Select selectBody){
        if(selectBody instanceof PlainSelect){
            List<Join> joins =((PlainSelect) selectBody).getJoins();
            return joins;
        }
        return new ArrayList<Join>();
    }

    /**
     *
     * @param selectBody
     * @return
     */
    public static List<Table> getIntoTables(Select selectBody){
        if(selectBody instanceof PlainSelect){
            List<Table> tables = ((PlainSelect) selectBody).getIntoTables();
            return tables;
        }
        return new ArrayList<Table>();
    }

    /**
     *
     * @param selectBody
     * @return
     */
    public static void setIntoTables(Select selectBody,List<Table> tables){
        if(selectBody instanceof PlainSelect){
            ((PlainSelect) selectBody).setIntoTables(tables);
        }
    }

    /**
     * 获取limit值
     * @param selectBody
     * @return
     */
    public static Limit getLimit(Select selectBody){
        if(selectBody instanceof PlainSelect){
            Limit limit = ((PlainSelect) selectBody).getLimit();
            return limit;
        }
        return null;
    }

    /**
     * 为SQL增加limit值
     * @param selectBody
     * @param l
     */
    public static void setLimit(Select selectBody,long l){
        if(selectBody instanceof PlainSelect){
            Limit limit = new Limit();
            limit.setRowCount(new LongValue(String.valueOf(l)));
            ((PlainSelect) selectBody).setLimit(limit);
        }
    }

    /**
     * 获取FromItem不支持子查询操作
     * @param selectBody
     * @return
     */
    public static FromItem getFromItem(Select selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            return fromItem;
        }else if(selectBody instanceof WithItem){
            SqlParserTool.getFromItem(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 获取子查询
     * @param selectBody
     * @return
     */
    public static Select getSubSelect(Select selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if(fromItem instanceof Select){
                return ((Select) fromItem);
            }
        }else if(selectBody instanceof WithItem){
            SqlParserTool.getSubSelect(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 判断是否为多级子查询
     * @param selectBody
     * @return
     */
    public static boolean isMultiSubSelect(Select selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if(fromItem instanceof Select){
                Select subBody = ((Select) fromItem).getSelectBody();
                if(subBody instanceof PlainSelect){
                    FromItem subFromItem = ((PlainSelect) subBody).getFromItem();
                    if(subFromItem instanceof Select){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取查询字段
     * @param selectBody
     * @return
     */
    public static List<SelectItem<?>> getSelectItems(Select selectBody){
        if(selectBody instanceof PlainSelect){
            List<SelectItem<?>> selectItems = ((PlainSelect) selectBody).getSelectItems();
            return selectItems;
        }
        return null;
    }

    public static void main(String[] args) throws JSQLParserException {
        String sql = "select * from (select userid from a) a";
        SqlType sqlType = SqlParserTool.getSqlType(sql);
        if(sqlType.equals(SqlType.SELECT)){
            Select statement = (Select) SqlParserTool.getStatement(sql);
            Select subSelect = SqlParserTool.getSubSelect(statement.getSelectBody());
            System.out.println(subSelect.getSelectBody());
        }
    }

}
