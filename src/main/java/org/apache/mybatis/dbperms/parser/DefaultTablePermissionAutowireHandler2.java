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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.dto.DataPermission;
import org.apache.mybatis.dbperms.dto.DataPermissionColumn;
import org.apache.mybatis.dbperms.dto.DataPermissionPayload;
import org.apache.mybatis.dbperms.dto.DataSpecialPermission;
import org.apache.mybatis.dbperms.utils.PatternFormatUtils;
import org.apache.mybatis.dbperms.utils.RandomString;
import org.apache.mybatis.dbperms.utils.SqlBuildUtils;
import org.apache.mybatis.dbperms.utils.StringUtils;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class DefaultTablePermissionAutowireHandler2 implements ITablePermissionAutowireHandler {
	
	protected static RandomString randomString = new RandomString(4);
	private BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider;
	private String alias = "t0";
	
	
	public DefaultTablePermissionAutowireHandler2(
			BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider) {
		this.permissionsProvider = permissionsProvider;
	}
	 
	
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String tableName) {
		// 查询数据权限
		Optional<DataPermissionPayload> permissionPayload = getPermissionsProvider().apply(metaHandler, tableName);
		if(null != permissionPayload && permissionPayload.isPresent()) {
			DataPermissionPayload payload = permissionPayload.get();
			// 普通权限
			if(CollectionUtils.isNotEmpty(payload.getPermissions())) {
				// 当前表对应的数据权限（可能有多列限制条件）
				List<DataPermission> permissionsList = payload.getPermissions().parallelStream()
		    				.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), tableName))
		    				.collect(Collectors.toList());
				// 单条限制规则
				if(CollectionUtils.isNotEmpty(permissionsList) && permissionsList.size() == 1) {
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
				
		    	// 进行判空
				if(CollectionUtils.isNotEmpty(permissionsList)) {
					// 构建限制条件
					List<String> conditionParts = new ArrayList<>();
					// 筛选出OR条件关联的筛选语句
					List<DataPermission> orPermissionsList = permissionsList.parallelStream()
						.filter(permission -> permission.getGroupRelation().compareTo(Relational.OR) == 0 )
	    				.collect(Collectors.toList());
					if(CollectionUtils.isNotEmpty(orPermissionsList)) {
						// 构建限制条件
						List<String> orConditionParts = new ArrayList<>();
						// 循环数据权限组
						for (DataPermission permission : orPermissionsList) {
							// 构造当前数据权限组内不同列的限制SQL
							List<String> columnParts = SqlBuildUtils.columnParts(alias, permission);
							if (CollectionUtils.isNotEmpty(columnParts)) {
								// 添加每列的限制条件
								if(columnParts.size() > 1) {
									orConditionParts.add(" ( " + StringUtils.join(columnParts, permission.getRelation().getOperator()) + " ) " );
								} else {
									orConditionParts.add(columnParts.get(0));
								}
							}		
						}
						if(CollectionUtils.isNotEmpty(orConditionParts)) {
							conditionParts.add(" ( " + orConditionParts.stream().collect(Collectors.joining(Relational.OR.getOperator())) + " ) ");
						}
					}
					
					// 筛选出AND条件关联的筛选语句
					List<DataPermission> andPermissionsList = permissionsList.parallelStream()
						.filter(permission -> permission.getGroupRelation().compareTo(Relational.AND) == 0 )
	    				.collect(Collectors.toList());
					if(CollectionUtils.isNotEmpty(andPermissionsList)) {
						// 循环数据权限组
						for (DataPermission permission : andPermissionsList) {
							// 构造当前数据权限组内不同列的限制SQL
							List<String> columnParts = SqlBuildUtils.columnParts(alias, permission);
							if (CollectionUtils.isNotEmpty(columnParts)) {
								// 添加每列的限制条件
								if(columnParts.size() > 1) {
									conditionParts.add(" ( " + StringUtils.join(columnParts, permission.getRelation().getOperator()) + " ) " );
								} else {
									conditionParts.add(columnParts.get(0));
								}
							}		
						}
						
					}
					
					if (CollectionUtils.isNotEmpty(conditionParts)) {
						StringBuilder builder = new StringBuilder();
						builder.append("(");
						builder.append("  SELECT ").append(alias).append(".* ");
						builder.append("  FROM ").append(tableName).append(" ").append(alias);
						builder.append(" WHERE ");
						builder.append(conditionParts.stream().collect(Collectors.joining(Relational.AND.getOperator())));
						builder.append(" ) ");
						return builder.toString();
					}
					
				}
				
			} 
			// 特殊权限
			if(CollectionUtils.isNotEmpty(payload.getSpecialPermissions())) {
				
				// 查找匹配的特殊权限
	        	Optional<DataSpecialPermission> optional = payload.getSpecialPermissions().stream()
						.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), tableName))
						.findFirst();	
	        	// 防空判断
	            if (optional.isPresent() && StringUtils.isNotBlank(optional.get().getSql())) {
	    			Map<String, String> variables = new HashMap<String, String>();
	    			// 数据对象表
	    			variables.put("table", optional.get().getTable());
	    			return PatternFormatUtils.format(optional.get().getSql(), variables);
	            }
				
			}
		}
		
		return null;
	}
	

	public BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> getPermissionsProvider() {
		return permissionsProvider;
	}
	
}
