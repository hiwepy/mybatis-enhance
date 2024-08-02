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
package org.apache.mybatis.enhance.dbperms.parser;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.enhance.dbperms.dto.DataPermission;
import org.apache.mybatis.enhance.dbperms.dto.DataPermissionColumn;
import org.apache.mybatis.enhance.dbperms.dto.DataPermissionPayload;
import org.apache.mybatis.enhance.dbperms.dto.DataSpecialPermission;
import org.apache.mybatis.enhance.utils.PatternFormatUtils;
import org.apache.mybatis.enhance.utils.RandomString;
import org.apache.mybatis.enhance.utils.SqlBuildUtils;
import org.apache.mybatis.enhance.utils.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class DefaultTablePermissionAutowireHandler implements ITablePermissionAutowireHandler {

	public static Pattern pattern_find = Pattern.compile("(?:(?:\\#\\{)([\\S]*?)(?:\\}))+", Pattern.CASE_INSENSITIVE);
	protected static RandomString randomString = new RandomString(4);
	private BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider;
	private String alias = "t0";

	public DefaultTablePermissionAutowireHandler(
			BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider) {
		this.permissionsProvider = permissionsProvider;
	}


	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String tableName) {
		// 查询数据权限
		Optional<DataPermissionPayload> permissionPayload = getPermissionsProvider().apply(metaHandler, tableName);
		if(null != permissionPayload && permissionPayload.isPresent()) {
			DataPermissionPayload payload = permissionPayload.get();
			// 构建限制条件
			List<String> conditionParts = new ArrayList<>();
			// 普通权限
			if(CollectionUtils.isNotEmpty(payload.getPermissions())) {

				// 当前表对应的数据权限（可能有多列限制条件）
				List<DataPermission> permissionsList = payload.getPermissions().parallelStream()
		    				.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), tableName))
		    				.collect(Collectors.toList());

				// 进行判空
				if(CollectionUtils.isEmpty(permissionsList)) {
					return null;
				}

				// 单条限制规则（优先处理SQL替换类型）
				if(permissionsList.size() == 1) {
					DataPermission permission = permissionsList.get(0);
					if (StringUtils.isNotBlank(permission.getSql()) && pattern_find.matcher(permission.getSql()).find()) {
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
						return PatternFormatUtils.format(permission.getSql(), variables);
					}
				}

				// 普通权限SQL
				conditionParts.add(SqlBuildUtils.conditionParts(alias, permissionsList));
			}
			// 特殊权限
			if(CollectionUtils.isNotEmpty(payload.getSpecialPermissions())) {

				// 查找匹配的特殊权限（可能有多列限制条件）
	        	List<DataSpecialPermission> permissionsList = payload.getSpecialPermissions().stream()
						.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), tableName) && CollectionUtils.isNotEmpty(permission.getColumns()))
						.collect(Collectors.toList());

	        	// 进行判空
				if(CollectionUtils.isNotEmpty(permissionsList)) {

					conditionParts.add(SqlBuildUtils.conditionSpecialParts(alias, permissionsList));

				} else {

					Optional<DataSpecialPermission> permissionsSpecial = payload.getSpecialPermissions().stream()
							.filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), tableName) && StringUtils.containsIgnoreCase(permission.getSql(), "SELECT"))
							.findFirst();
					// 单个限制规则（优先处理SQL替换类型）
					if(permissionsSpecial.isPresent()) {
						DataSpecialPermission permission = permissionsSpecial.get();
						Map<String, String> variables = new HashMap<String, String>();
		    			// 数据对象表
		    			variables.put("table", permission.getTable());
		    			return PatternFormatUtils.format(permission.getSql(), variables);
					}

				}

			}

			conditionParts = conditionParts.parallelStream().filter(item -> !Objects.isNull(item)).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(conditionParts)) {

				StringBuilder builder = new StringBuilder();
				builder.append("(");
				builder.append("  SELECT ").append(alias).append(".* ");
				builder.append("  FROM ").append(tableName).append(" ").append(alias);
				builder.append(" WHERE ");
				builder.append(conditionParts.stream().collect(Collectors.joining(payload.getRelation().getOperator())));
				builder.append(" ) ");
				return builder.toString();
			}
		}

		return null;
	}

	public BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> getPermissionsProvider() {
		return permissionsProvider;
	}

}
