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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionParser;
import org.apache.mybatis.dbperms.parser.ITablePermissionScriptHandler;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TablePermissionScriptParser implements ITablePermissionParser {
	
	private static Pattern scriptPattern = Pattern.compile("(?:(?:\\{)([^\\{\\}]*?)(?:\\}))+");
	private ITablePermissionScriptHandler tablePermissionHandler;
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
    
    public String parser(MetaStatementHandler metaHandler, String originalSQL) {
    	if (!this.doFilter(metaHandler, originalSQL)) {
    		 return originalSQL;
		}
    	
    	this.init();
    	
    	// 匹配全部SQL，查找数据权限脚本{}片段
    	Matcher matcher = scriptPattern.matcher(originalSQL);
		while (matcher.find()) {
			
			// 获取匹配的{}的内容
			String fullSegment = matcher.group(0);
			// {} 中间的内容
			String segmentSQL = matcher.group(1);
			// 取得{}内容开始结束位置
			int begain = originalSQL.indexOf(fullSegment);
			int end = begain + fullSegment.length();
			
			// 处理权限脚本片段
			Optional<String> optional = getTablePermissionHandler().process(metaHandler, segmentSQL);
			// 如果有权限
			if(optional.isPresent()) {
				originalSQL = originalSQL.substring(0, begain) + optional.get() + originalSQL.substring(end);
			} else {
				originalSQL = originalSQL.substring(0, begain) + " " + originalSQL.substring(end);
			}
			
		}
		return originalSQL;
    }
    
}
