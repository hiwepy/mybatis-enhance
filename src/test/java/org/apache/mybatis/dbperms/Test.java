/**
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
package org.apache.mybatis.dbperms;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mybatis.dbperms.annotation.Condition;
import org.apache.mybatis.dbperms.annotation.ForeignCondition;
import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionColumn;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionForeign;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.QueryTablesNamesFinder;

public class Test {
	 
	
	/**
	 * 自动注入方式
	 */
	@RequiresPermissions
	public void test1() {
		
	}
	
	/**
	 * 手动定义方式
	 */
	@RequiresPermissions(
		autowire = false,
		value = {
			@RequiresPermission(table = "XXX_XXB", value = {
				@RequiresPermissionColumn(column = "R_ID", condition = Condition.IN, perms = "1001,1002"),
				@RequiresPermissionColumn(column = "AGE", condition = Condition.BITAND_EQ, perms = "15")
			}, relation =  Relational.AND),
			@RequiresPermission(table = "YYY_XXB", value = {
				@RequiresPermissionColumn(column = "R_ID", condition = Condition.IN, perms = "1001,1002"),
				@RequiresPermissionColumn(column = "AGE", condition = Condition.BITAND_EQ, perms = "15")
			}, relation =  Relational.OR)
		}
	)
	public void test2() {
		
	}
	
	/**
	 * 手动定义方式
	 */
	@RequiresPermission(table = "XXX_XXB", value = {
		@RequiresPermissionColumn(column = "R_ID", condition = Condition.IN, perms = "1001,1002"),
		@RequiresPermissionColumn(column = "AGE", condition = Condition.BITAND_EQ, perms = "15"),
		@RequiresPermissionColumn(column = "P_ID", condition = Condition.EXISTS, perms = "15", 
			foreign = @RequiresPermissionForeign( table = "xxxxx", column = "xxx", condition = ForeignCondition.EQ)
		)
	}, relation =  Relational.AND)
	public void test3() {
		
	}
	
	public static void main(String[] args) throws Exception {
		
		String originalSQL = "select t.xh,t.xsjbxxb_id,t.xqdmb_id,t.ssxy_id,t.zyfxdmb_id,t.zyh_id,t.xzbdmb_id,t.nj_id from xs_xsjbxxb t，xs_xsjbxxb 2";
		Pattern pattern_find = Pattern.compile("(xs_xsjbxxb)+");
    	// 匹配所有匹配的表名
		Matcher matcher = pattern_find.matcher(originalSQL);
		// 查找匹配的片段
		while (matcher.find()) {
			// 获取匹配的内容
			String full_segment = matcher.group(0);
			// 取得{}内容开始结束位置
			int begain = originalSQL.indexOf(full_segment);
			int end = begain + full_segment.length();
			
			originalSQL = originalSQL.substring(0, begain) + "xxxxxxxxxxxxxxxxxxx" + originalSQL.substring(end);
			
			
			System.out.println(full_segment);
			System.out.println(begain);
			System.out.println(end);
			
		}
		
		String sql ="		SELECT\r\n" + 
				"			t1.ID,\r\n" + 
				"			t1.XH,\r\n" + 
				"			t1.XM,\r\n" + 
				"			t1.YWXM,\r\n" + 
				"			t1.XMPY,\r\n" + 
				"			t1.CYM,\r\n" + 
				"			t1.XBM,\r\n" + 
				"			t1.CSRQ,\r\n" + 
				"			t1.CSDM,\r\n" + 
				"			t1.JG,\r\n" + 
				"			t1.MZM,\r\n" + 
				"			t1.GJDQM,\r\n" + 
				"			t1.SFZJLXM,\r\n" + 
				"			t1.SFZJH,\r\n" + 
				"			t1.HYZKM,\r\n" + 
				"			t1.GATQWM,\r\n" + 
				"			t1.ZZMMM,\r\n" + 
				"			t1.JKZKM,\r\n" + 
				"			t1.XYZJM,\r\n" + 
				"			t1.XXM,\r\n" + 
				"			t1.ZP,\r\n" + 
				"			t1.DH,\r\n" + 
				"			t1.YDDH,\r\n" + 
				"			t1.CZDH,\r\n" + 
				"			t1.DZXX,\r\n" + 
				"			t1.WLDZ,\r\n" + 
				"			t1.WBZH,\r\n" + 
				"			t1.QQHM,\r\n" + 
				"			t1.WXHM,\r\n" + 
				"			t1.SFZJYXQ,\r\n" + 
				"			t1.SFDSZN,\r\n" + 
				"			t1.YXXMC,\r\n" + 
				"			t1.YXH,\r\n" + 
				"			t1.RXFSM,\r\n" + 
				"			t1.LYDQM,\r\n" + 
				"			t1.XSLYM,\r\n" + 
				"			t1.JDFSM,\r\n" + 
				"			t1.JTZZ,\r\n" + 
				"			t1.JTYZBM,\r\n" + 
				"			t1.JTDH,\r\n" + 
				"			t1.JTDZXX,\r\n" + 
				"			t1.JTCYRS,\r\n" + 
				"			t1.JTNSR,\r\n" + 
				"			t1.BZXX,\r\n" + 
				"			t1.KSH,\r\n" + 
				"			t2.SZNJ,\r\n" + 
				"			t2.BJMC,\r\n" + 
				"			t2.ZYMC,\r\n" + 
				"			t2.DWMC,\r\n" + 
				"			t2.classId,\r\n" + 
				"			t2.majorId,\r\n" + 
				"			t2.DWH,\r\n" + 
				"			t2.collegeId,\r\n" + 
				"			t1.TCAH,\r\n" + 
				"			(SELECT D1.D_TEXT FROM SYS_EXTRAS_PAIRVALUE D1 WHERE D1.D_KEY = t2.XJZT AND D1.D_GROUP = 'XJZT' ) AS XJZT\r\n" + 
				"		FROM GXXS_XSXXB t1\r\n" + 
				"		 LEFT JOIN (SELECT gs.XSID,\r\n" + 
				"                    gs.SZNJ,\r\n" + 
				"                    class.ID     AS classId,\r\n" + 
				"                    class.BJMC,\r\n" + 
				"                    major.ID     AS majorId,\r\n" + 
				"                    major.ZYMC,\r\n" + 
				"                    college.DWMC,\r\n" + 
				"                    college.DWH,\r\n" + 
				"                    college.ID   AS collegeId,\r\n" + 
				"                    gs.xjzt\r\n" + 
				"               FROM GXXS_XJZTXXB gs\r\n" + 
				"               LEFT JOIN GXXX_ZYXXB major ON major.id = gs.ZYID\r\n" + 
				"               LEFT JOIN GXXX_XYXXB college ON college.id = gs.YXID\r\n" + 
				"               LEFT JOIN GXXX_BJXXB class ON class.ID = gs.BJID\r\n" + 
				"         ) t2  ON t1.ID = t2.XSID";

		Statement statement = CCJSqlParserUtil.parse(sql);

//		            Select selectStatement = (Select)statement;

		QueryTablesNamesFinder tablesNamesFinder = new QueryTablesNamesFinder();
		List<String> result = tablesNamesFinder.getTableList(statement);
		for(String tableStr : result){

			System.out.println(">>>> "+ tableStr);

		}
		
		System.out.println(originalSQL);
		
		
	}
	
}
