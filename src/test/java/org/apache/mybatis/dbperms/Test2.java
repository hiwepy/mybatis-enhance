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
package org.apache.mybatis.dbperms;


import org.apache.ibatis.exception.MybatisException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class Test2 {

public static void main(String[] args) {
		
		String sql = "select m.id as jsdmid, " + 
				"                m.dmrq, " + 
				"                m.dmkssj, " + 
				"                m.dmjssj, " + 
				"                m.dmfs, " + 
				"                m.gzid, " + 
				"                m.fqdmzb, " + 
				"                m.fqdmdd, " + 
				"                m.cjsj, " + 
				"        m.gzmc as xsmc, " + 
				"        n.bjid, " + 
				"        TO_CHAR(ROUND(decode(p.zrs, 0, 0, p.qdrs /p.zrs*100), 2),'fm9999999990.00')||'%' as qdl " + 
				"        from GXXS_KQ_DMGZJLB m, " + 
				"        	  GXXS_KQ_DMGZB n, " + 
				"        	  ( " + 
				"        	select count(t1.id) as zrs, " + 
				"        	sum(case when dmzt=1 then 1 else 0 end) as qdrs, " + 
				"        	t1.jsdmid " + 
				"        	from GXXS_KQ_DMJLB t1 " + 
				"        	group by t1.jsdmid " + 
				"        ) p " + 
				"        where m.gzid = n.id " + 
				"		    AND p.jsdmid=m.id " +
				"		    AND m.sfzdy = '1' " + 
				"           AND m.jsid = ? " + 
				"           AND (to_date(m.dmrq||' '||m.dmkssj||':00','yyyy-mm-dd hh24:mi:ss') " + 
				"            BETWEEN to_date(?||':00','yyyy-mm-dd hh24:mi:ss') " + 
				"            AND to_date(?||':59','yyyy-mm-dd hh24:mi:ss')) ";
		
		try {
			
            Statement statement = CCJSqlParserUtil.parse(sql, (parser) -> {
            
            	try {
            		
					System.err.println(parser.getParseErrors());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
            	
            });
            
            if (null != statement && statement instanceof Select) { 
            	Select select = (Select) statement;
            	 
            }
		} catch (JSQLParserException e) {
			throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
		}
		
	}

}
