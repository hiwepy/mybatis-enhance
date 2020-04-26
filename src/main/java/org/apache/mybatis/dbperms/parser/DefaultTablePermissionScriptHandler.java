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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.dto.DataPermission;
import org.apache.mybatis.dbperms.dto.DataPermissionColumn;
import org.apache.mybatis.dbperms.dto.DataPermissionForeign;
import org.apache.mybatis.dbperms.dto.DataPermissionPart;
import org.apache.mybatis.dbperms.dto.DataPermissionPayload;
import org.apache.mybatis.dbperms.utils.DataPermissionScriptUtils;
import org.apache.mybatis.dbperms.utils.PatternFormatUtils;
import org.apache.mybatis.dbperms.utils.RandomString;
import org.apache.mybatis.dbperms.utils.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 根据SQL中的特殊脚本的权限信息组装权限语句
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class DefaultTablePermissionScriptHandler implements ITablePermissionScriptHandler {
	
	protected static RandomString randomString = new RandomString(4);
	private static Pattern inPattern = Pattern.compile("(?:(?:in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+");
	private static Pattern existsPattern = Pattern.compile("(?:(?:exists\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+");
	private static Pattern notExistsPattern = Pattern.compile("(?:(?:not-exists\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+");
	private static Pattern notInPattern = Pattern.compile("(?:(?:not-in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+");
	private static Pattern bitandPattern = Pattern.compile("(?:(?:bitand\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+");
	private BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider;
	
	public DefaultTablePermissionScriptHandler(
			BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider) {
		this.permissionsProvider = permissionsProvider;
	}
	
	protected void resolved(MetaStatementHandler metaHandler, DataPermissionPart resolved, Function<List<DataPermission>, String> func) {

		// 查询数据权限
		Optional<DataPermissionPayload> permissionPayload = getPermissionsProvider().apply(metaHandler, resolved.getTable());
		if(null != permissionPayload && permissionPayload.isPresent()) {
			DataPermissionPayload payload = permissionPayload.get();
			// 普通权限
			if(!CollectionUtils.isEmpty(payload.getPermissions())) {
				
				// 当前表对应的数据权限
				List<DataPermission> permissionsList = payload.getPermissions().stream()
		    				.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), resolved.getTable()))
		    				.collect(Collectors.toList());
		    	// 进行判空
				if(CollectionUtils.isEmpty(permissionsList)) {
					// 单条限制规则
					if(permissionsList.size() == 1) {
						DataPermission permission = permissionsList.get(0);
						String trans = permission.getSql();
						if (StringUtils.isNotBlank(trans)) {
							Map<String, String> variables = new HashMap<String, String>();
							// 数据对象表
							variables.put("table", tableName);
							// 字段限制值
							for (DataPermissionColumn column : permission.getColumns()) {
								if(!StringUtils.isNotBlank(column.getPerms())) {
									continue;
								}
								// 字段对应的变量
								String[] permsArr = StringUtils.split(column.getPerms(),",");
								if(permsArr.length == 1) {
									variables.put(StringUtils.lowerCase(column.getColumn()), permsArr[0]);
								} else {
									variables.put(StringUtils.lowerCase(column.getColumn()), Stream.of(permsArr)
											.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(",")));
								}
							}
							return PatternFormatUtils.format(trans, variables);
						} 
					}
					
					// 构建限制条件
					List<String> allAolumnParts = new ArrayList<String>();
					for (DataPermission permission : permissionsList) {
						List<String> columnParts = this.columnParts(alias, permission);
						if (CollectionUtils.isEmpty(columnParts)) {
							// 添加每列的限制条件
							allAolumnParts.add(StringUtils.join(columnParts, permission.getRelation().getOperator()));
						}
					}
					if (CollectionUtils.isEmpty(allAolumnParts)) {
						StringBuilder builder = new StringBuilder();
						builder.append("(");
						builder.append("  SELECT ").append(alias).append(".* ");
						builder.append("  FROM ").append(tableName).append(" ").append(alias);
						
						builder.append(" WHERE ");
						if(allAolumnParts.size() > 1) {
							builder.append(allAolumnParts.stream().map(part -> " ( " + part + " ) ").collect(Collectors.joining(Relational.OR.getOperator())));
						} else {
							builder.append(allAolumnParts.get(0));
						}
						builder.append(")");
						return builder.toString();
					}
					
				}
				
			} 
		}
	}
	 
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String segment) {
		
		//是否替换的标记
		boolean replace = true;
		// 对func内容进行解析
		String dataRangeSQL = "";
		// 匹配每段数据范围规则内容中的in()
		Matcher inMatcher = inPattern.matcher(segment);
		// 匹配每段数据范围规则内容中的exists()
		Matcher existsMatcher = existsPattern.matcher(segment);
		// 匹配每段数据范围规则内容中的not-exists()
		Matcher notExistsMatcher = notExistsPattern.matcher(segment);
		// 匹配每段数据范围规则内容中的not-in()
		Matcher notInMatcher = notInPattern.matcher(segment);
		// 匹配每段数据范围规则内容中的bitand()
		Matcher bitandMatcher = bitandPattern.matcher(segment);
		
		// 不使用while,这里只匹配一次in
		if (inMatcher.find()) { 
			
			// 拼接原func前SQL；如: t.nj in
			int begainIndex = segment.indexOf("in");
			String begainSQL = segment.substring(0, begainIndex);
			//在{}区域内符合当前正则表达式的整段字符
			String segmentStr = inMatcher.group(0);
			// 获取()中间的内容
			String ruleStr = inMatcher.group(1);

			DataPermissionPart resolved = DataPermissionScriptUtils.getResolvedPart("in", ruleStr);
		
			//去除func()部分后的字符内容
			String sqlStr  = segment.substring(0, begainIndex) + segment.substring(begainIndex + segmentStr.length());
			/*
			 * 1. 	{func("jw_xjgl_xsxjxxb","xm","bj","bh_id","xsxm")} 模式表示 使用 内连接模式，处理逻辑为包裹原SQL，与筛选表组成全连接查询
			 */
			if (sqlStr.trim().length() == 0) {
				// 添加标记；表示非替换处理
				replace = false;
				// 解析func的表名或视图名,匹配字段,表字段,数据范围需过滤的条件字段
				matcherFuncs.add(getResolvedMap("func",ruleStr));
			}
			/*
			 * 2. 	{ t.xh in func('xs_xsjbxxb';'xh';'ssxy_id';'jg_id') } 模式表示 使用 in 关键字，处理逻辑为用解析的SQL 替换匹配区域字段
			 * 		{ instr[a.xx_id,func('xs_xsjbxxb';'xh';'ssxy_id';'jg_id')] > 0 }
			 * 		{ func('xs_xsjbxxb';'wm_concat[xh]';'ssxy_id';'jg_id') like '%'|| t.xh ||'%' }
			 */
			else {
				// 添加标记；表示替换处理
				replace = true;
				// 在循环的过程中，替换掉当前的片段
				dataRangeSQL = getFragmentSQL(matcherFunc , segment,dataRangeFields , dataRanges , allCollegeDataRanges, defaultLimts);
			}
			
		} else if (matcherBitand.find()) {
			// 在循环的过程中，替换掉当前的片段
			dataRangeSQL = getPartBitandSQL(matcherBitand, segment, dataRangeFields , dataRanges, allCollegeDataRanges, defaultLimts);
			// 添加标记；表示替换处理
			replace = true;
		}
		
		// 如果是替换处理，则在每次的循环中进行处理SQL
		if (true == replace) {
			
			//SQL片段不为空
			if(!StringUtils.isBlank(dataRangeSQL)){
				//得到当前条件片段之前的sql,并去除换行空格等
				//.replaceAll("(\r \n(\\s*\r \n)+)", "\r\n").replaceAll(" +","");
				String tmp = originalSQL.substring(0, begain).replaceAll("[\\s]+", " ").trim();
				//判断当前条件前面SQL是否以where结尾
				if( !tmp.toLowerCase().endsWith("where")){
					dataRangeSQL =  " and " + dataRangeSQL;
				}
			}
			originalSQL = originalSQL.substring(0, begain) + dataRangeSQL + originalSQL.substring(end);
			index += 1;
		}else{
			//内连接模式；去除规则func部分
			originalSQL = originalSQL.substring(0, begain) + " " + originalSQL.substring(end);
		}
		
		// 得到当前条件片段之前的sql,并去除换行空格等
		String tmp = originalSQL.replaceAll("[\\s]+", " ").trim();
		// 判断当前条件前面SQL是否以where结尾
		if(tmp.toLowerCase().endsWith("where")){
			originalSQL = originalSQL.substring(0, originalSQL.toLowerCase().lastIndexOf("where"));
		}
		// 将原使用[]符号表示的函数重新转换成数据库可识别的函数
		return originalSQL.replace("[", "(").replace("]", ")");
	}
	
	List<String> columnParts(String alias, DataPermission permission){
		// 构建数据限制条件SQL
		List<String> columnParts = new ArrayList<String>();
		for (DataPermissionColumn column : permission.getColumns()) {
			if(null == column.getPerms()) {
				continue;
			}
			// 数据权限值数组
			String[] permsArr = StringUtils.split(column.getPerms(),",");
			switch (column.getCondition()) {
				case GT:
				case GTE:
				case LT:
				case LTE:
				case EQ:
				case NE:
				case LIKE:
				case LIKE_LEFT:
				case LIKE_RIGHT:{
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.getCondition().getOperator(), alias, column.getColumn(), StringUtils.quote(permsArr[0])));
					} else {	
						StringBuilder partSQL = new StringBuilder();
						partSQL.append(" ( ");
						partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
								.map(perm -> String.format(column.getCondition().getOperator(), alias, column.getColumn(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						columnParts.add(partSQL.toString());
					}
				};break;
				case IN:{
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.getCondition().getOperator(), alias, column.getColumn(), StringUtils.quote(permsArr[0])));
					} else {
						String inPart = Stream.of(StringUtils.split(column.getPerms(),","))
								.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
						columnParts.add(String.format(column.getCondition().getOperator(), alias, column.getColumn(), inPart));
					}
				};break;
				case BITAND_GT:
				case BITAND_GTE:
				case BITAND_LT:
				case BITAND_LTE:
				case BITAND_EQ:{
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.getCondition().getOperator(), Integer.parseInt(permsArr[0]), alias, column.getColumn()));
					} else {
						StringBuilder partSQL = new StringBuilder();
						partSQL.append(" ( ");
						partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
								.map(perm -> String.format(column.getCondition().getOperator(), Integer.parseInt(perm), alias, column.getColumn())).collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						columnParts.add(partSQL.toString());
					}
				};break;
				case EXISTS:
				case NOT_EXISTS:{
					DataPermissionForeign foreign = column.getForeign();
					if(null != foreign){
						StringBuilder partSQL = new StringBuilder();
						// 开始构建关联SQL
						partSQL.append(" SELECT ").append(" fkt.").append(foreign.getColumn());
						partSQL.append(" FROM ").append(foreign.getTable()).append(" fkt ");
						partSQL.append(" WHERE ").append(" fkt.").append(foreign.getColumn()).append(" = ").append(alias).append(column.getColumn());
						// 构建两个表关联关系条件SQL
						switch (foreign.getCondition()) {
						case GT:
						case GTE:
						case LT:
						case LTE:
						case EQ:
						case NE:
						case LIKE:
						case LIKE_LEFT:
						case LIKE_RIGHT:{
							if(permsArr.length == 1) {
								partSQL.append(" AND ").append(String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), StringUtils.quote(permsArr[0])));
							} else {	
								partSQL.append(" AND ( ");
								partSQL.append(Stream.of(permsArr)
										.map(perm -> String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), StringUtils.quote(perm)))
										.collect(Collectors.joining(" OR ")));
								partSQL.append(" ) ");
							}
						};break;
						case IN:{
							if(permsArr.length == 1) {
								partSQL.append(" AND ").append(String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), StringUtils.quote(permsArr[0])));
							} else {	
								String inPart = Stream.of(permsArr)
										.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
								partSQL.append(" AND ").append(String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), inPart));
							}
						};break;
						case BITAND_GT:
						case BITAND_GTE:
						case BITAND_LT:
						case BITAND_LTE:
						case BITAND_EQ:{
							if(permsArr.length == 1) {
								partSQL.append(" AND ").append(String.format(foreign.getCondition().getOperator(), Integer.parseInt(permsArr[0]), "fkt", foreign.getColumn()));
							} else {	
								partSQL.append(" AND ( ");
								partSQL.append(Stream.of(permsArr)
										.map(perm -> String.format(foreign.getCondition().getOperator(), Integer.parseInt(perm), "fkt", foreign.getColumn()))
										.collect(Collectors.joining(" OR ")));
								partSQL.append(" ) ");
							}
						};break;
						default:{};break;
						}
						columnParts.add(String.format(column.getCondition().getOperator(), partSQL.toString()));
					}
				};break;
				default:{};break;
			}
		}
		return columnParts;
	}

	public BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> getPermissionsProvider() {
		return permissionsProvider;
	}
	
}
