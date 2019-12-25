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
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.interceptor.DataPermission;
import org.apache.mybatis.dbperms.interceptor.DataPermissionColumn;
import org.apache.mybatis.dbperms.interceptor.DataPermissionForeign;
import org.apache.mybatis.dbperms.utils.RandomString;
import org.apache.mybatis.dbperms.utils.StringUtils;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/vindell">vindell</a>
 */
public class DefaultTablePermissionAutowireHandler implements ITablePermissionAutowireHandler {
	
	protected static RandomString randomString = new RandomString(4);
	private BiFunction<MetaStatementHandler, String, Optional<List<DataPermission>>> permissionsProvider;
	
	public DefaultTablePermissionAutowireHandler(
			BiFunction<MetaStatementHandler, String, Optional<List<DataPermission>>> permissionsProvider) {
		this.permissionsProvider = permissionsProvider;
	}
	
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String tableName) {
		
		Optional<List<DataPermission>> permissions = permissionsProvider.apply(metaHandler, tableName);
		if(null != permissions && permissions.isPresent()) {
			List<DataPermission> permissionsList = permissions.get();
			if(CollectionUtils.isNotEmpty(permissionsList)) {
				// 单条限制规则
				if(permissionsList.size() == 1) {
					DataPermission permission = permissionsList.get(0);
					String wrapSQL = permission.getWrapSQL();
					if (StringUtils.isNotBlank(wrapSQL)) {
						
					} 
				}
			}
			
			int tindex = 0;
			String alias = "t" + tindex;
			
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append("SELECT ").append(alias).append(".* ");
			builder.append("FROM ").append(tableName).append(" ").append(alias);
			// 构建限制条件
			List<String> columnParts = new ArrayList<String>();
			for (DataPermission permission : permissions.get()) {
				
				// 构建数据限制条件SQL
				List<String> parts = new ArrayList<String>();
				for (DataPermissionColumn column : permission.getColumns()) {
					if(!StringUtils.isNotBlank(column.getPerms())) {
						continue;
					}
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
							StringBuilder partSQL = new StringBuilder();
							partSQL.append(" ( ");
							partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
									.map(perm -> String.format(column.getCondition().getOperator(), alias, column.getColumn(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
							partSQL.append(" ) ");
							parts.add(partSQL.toString());
						};break;
						case IN:{
							String inPart = Stream.of(StringUtils.split(column.getPerms(),","))
								.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
							parts.add(String.format(column.getCondition().getOperator(), alias, column.getColumn(), inPart));
						};break;
						case BITAND_GT:
						case BITAND_GTE:
						case BITAND_LT:
						case BITAND_LTE:
						case BITAND_EQ:{
							StringBuilder partSQL = new StringBuilder();
							partSQL.append(" ( ");
							partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
									.map(perm -> String.format(column.getCondition().getOperator(), Integer.parseInt(perm), alias, column.getColumn())).collect(Collectors.joining(" OR ")));
							partSQL.append(" ) ");
							parts.add(partSQL.toString());
						};break;
						case EXISTS:
						case NOT_EXISTS:{
							DataPermissionForeign foreign = column.getForeign();
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
									partSQL.append(" AND ( ");
									partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
											.map(perm -> String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								};break;
								case IN:{
									String inPart = Stream.of(StringUtils.split(column.getPerms(),","))
										.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
									partSQL.append(String.format(foreign.getCondition().getOperator(), "fkt", foreign.getColumn(), inPart));
								};break;
								case BITAND_GT:
								case BITAND_GTE:
								case BITAND_LT:
								case BITAND_LTE:
								case BITAND_EQ:{
									partSQL.append(" AND ( ");
									partSQL.append(Stream.of(StringUtils.split(column.getPerms(),","))
											.map(perm -> String.format(foreign.getCondition().getOperator(), Integer.parseInt(perm), "fkt", foreign.getColumn())).collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								};break;
								default:{};break;
							}
							parts.add(String.format(column.getCondition().getOperator(), partSQL.toString()));
						};break;
						default:{};break;
					}
					tindex ++;
				}
				// 添加每列的限制条件
				columnParts.add(StringUtils.join(parts, permission.getRelation().getOperator()));
			}
			
			if(columnParts.size() > 0) {
				builder.append(" WHERE ");
				if(columnParts.size() > 1) {
					builder.append(columnParts.stream().map(part -> " ( " + part + " ) ").collect(Collectors.joining(Relational.OR.getOperator())));
				} else {
					builder.append(columnParts.get(0));
				}
			}
			
			builder.append(")");
			
			return builder.toString();
		}
		
		return null;
	}

}
