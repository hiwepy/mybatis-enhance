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
package org.apache.mybatis.dbperms.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.dto.DataPermission;
import org.apache.mybatis.dbperms.dto.DataPermissionColumn;
import org.apache.mybatis.dbperms.dto.DataPermissionPart;
import org.apache.mybatis.dbperms.dto.DataPermissionPayload;
import org.apache.mybatis.dbperms.utils.RandomString;
import org.apache.mybatis.dbperms.utils.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 根据SQL中的特殊脚本的权限信息组装权限语句
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class DefaultTablePermissionScriptHandler implements ITablePermissionScriptHandler {
	
	protected static RandomString randomString = new RandomString(4);
	private BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider;
	private static Map<Pattern, String> patternMap = new HashMap<>();
	
	static {
		// { x.id in (表,字段) }
		patternMap.put(Pattern.compile("(?:(?:in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+"), " in (%s) ");
		patternMap.put(Pattern.compile("(?:(?:not-in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+"), " not in (%s) ");
		
	}
	
	public DefaultTablePermissionScriptHandler(
			BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider) {
		this.permissionsProvider = permissionsProvider;
	}
	
	protected List<String> resolved(MetaStatementHandler metaHandler, DataPermissionPart resolved) {
		// 查询数据权限
		Optional<DataPermissionPayload> permissionPayload = getPermissionsProvider().apply(metaHandler, resolved.getTable());
		List<String> rtList = new ArrayList<>();
		if(null != permissionPayload && permissionPayload.isPresent()) {
			DataPermissionPayload payload = permissionPayload.get();
			// 普通权限
			if(!CollectionUtils.isEmpty(payload.getPermissions())) {
				// 当前表对应的数据权限
				List<DataPermission> permissionsList = payload.getPermissions().parallelStream()
		    				.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), resolved.getTable()))
		    				.collect(Collectors.toList());
		    	// 进行判空
				if(CollectionUtils.isEmpty(permissionsList)) {
					for (DataPermission permission : permissionsList) {
						for (DataPermissionColumn column : permission.getColumns()) {
							if(null == column.getPerms() && !StringUtils.equalsIgnoreCase(column.getColumn(), resolved.getRelated())) {
								continue;
							}
							// 数据权限值数组
							String[] permsArr = StringUtils.split(column.getPerms(),",");
							for (String perms : permsArr) {
								rtList.add(perms);
							}
						}
					}
				}
			} 
		}
		return rtList;
	}
	 
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String segmentSQL) {
		
		Iterator<Entry<Pattern, String>> ite = patternMap.entrySet().iterator();
		while (ite.hasNext()) {
			
			Entry<Pattern, String> entry = ite.next();
			
			Matcher matcher = entry.getKey().matcher(segmentSQL);
			
			// 不使用while,这里只匹配一次in
			if (matcher.find()) {
				
				// 获取匹配的in()的内容
				String fullSegment = matcher.group(0);
				// () 中间的内容
				String segment = matcher.group(1);
				// 取得()内容开始结束位置
				int begain = segmentSQL.indexOf(fullSegment);
				int end = begain + fullSegment.length();

				DataPermissionPart resolved = new DataPermissionPart();
				String[] ruleStrs = StringUtils.split(segment, ";");
				resolved.setTable(ruleStrs[0]);
				resolved.setRelated(ruleStrs[1]);
				
				List<String> permsList = this.resolved(metaHandler, resolved);
				if(!CollectionUtils.isEmpty(permsList)) {
					String part = permsList.parallelStream().map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
					segmentSQL = segmentSQL.substring(0, begain) + part + segmentSQL.substring(end);
				}
				
				return null;
				 
			}
		}
		
		// 得到当前条件片段之前的sql,并去除换行空格等
		String tmp = segmentSQL.replaceAll("[\\s]+", " ").trim();
		// 判断当前条件前面SQL是否以where结尾
		if(tmp.toLowerCase().endsWith("where")){
			segmentSQL = segmentSQL.substring(0, segmentSQL.toLowerCase().lastIndexOf("where"));
		}
		// 将原使用[]符号表示的函数重新转换成数据库可识别的函数
		return segmentSQL.replace("[", "(").replace("]", ")");
	}

	public BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> getPermissionsProvider() {
		return permissionsProvider;
	}
	
}
