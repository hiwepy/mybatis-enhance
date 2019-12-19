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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionColumn;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionForeign;
import org.apache.mybatis.dbperms.utils.StringUtils;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/vindell">vindell</a>
 */
public class DefaultTablePermissionAnnotationHandler implements ITablePermissionAnnotationHandler {

	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresPermission permission) {
		RequiresPermissionColumn[] columns = permission.value();
		if(ArrayUtils.isNotEmpty(columns)) {
			
			int tindex = 0;
			String alias = "t" + tindex;
			
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(" SELECT ").append(alias).append(".*");
			builder.append(" FROM ").append(permission.table()).append(" ").append(alias);
			// 构建数据限制条件SQL
			List<String> parts = new ArrayList<String>();
			for (RequiresPermissionColumn column : columns) {
				
				if(!StringUtils.isNotBlank(column.perms())) {
					continue;
				}
				switch (column.condition()) {
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
						partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
								.map(perm -> String.format(column.condition().getOperator(), alias, column.column(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						parts.add(partSQL.toString());
					};break;
					case IN:{
						String inPart = Stream.of(StringUtils.split(column.perms(),","))
							.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
						parts.add(String.format(column.condition().getOperator(), alias, column.column(), inPart));
					};break;
					case BITAND_GT:
					case BITAND_GTE:
					case BITAND_LT:
					case BITAND_LTE:
					case BITAND_EQ:{
						StringBuilder partSQL = new StringBuilder();
						partSQL.append(" ( ");
						partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
								.map(perm -> String.format(column.condition().getOperator(), Integer.parseInt(perm), alias, column.column())).collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						parts.add(partSQL.toString());
					};break;
					case EXISTS:
					case NOT_EXISTS:{
						RequiresPermissionForeign foreign = column.foreign();
						StringBuilder partSQL = new StringBuilder();
						// 开始构建关联SQL
						partSQL.append(" SELECT ").append(" fkt.").append(foreign.column());
						partSQL.append(" FROM ").append(foreign.table()).append(" fkt ");
						partSQL.append(" WHERE ").append(" fkt.").append(foreign.column()).append(" = ").append(alias).append(column.column());
						// 构建两个表关联关系条件SQL
						switch (foreign.condition()) {
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
								partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
										.map(perm -> String.format(foreign.condition().getOperator(), "fkt", foreign.column(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
								partSQL.append(" ) ");
							};break;
							case IN:{
								String inPart = Stream.of(StringUtils.split(column.perms(),","))
									.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
								partSQL.append(String.format(foreign.condition().getOperator(), "fkt", foreign.column(), inPart));
							};break;
							case BITAND_GT:
							case BITAND_GTE:
							case BITAND_LT:
							case BITAND_LTE:
							case BITAND_EQ:{
								partSQL.append(" AND ( ");
								partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
										.map(perm -> String.format(foreign.condition().getOperator(), Integer.parseInt(perm), "fkt", foreign.column())).collect(Collectors.joining(" OR ")));
								partSQL.append(" ) ");
							};break;
							default:{};break;
						}
						parts.add(String.format(column.condition().getOperator(), partSQL.toString()));
					};break;
					default:{};break;
				}
				tindex ++;
			}
			builder.append(" WHERE ").append(StringUtils.join(parts, permission.relation().toString() ));
			builder.append(" )");
			
			return builder.toString();
		}
		
		return null;
	}

	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, List<RequiresPermission> permissions) {
		
		if(CollectionUtils.isNotEmpty(permissions)) {

			RequiresPermission permission0 = permissions.get(0);
			
			int tindex = 0;
			String alias = "t" + tindex;
			
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append("SELECT ").append(alias).append(".* ");
			builder.append("FROM ").append(permission0.table()).append(" ").append(alias);
			// 构建限制条件
			List<String> columnParts = new ArrayList<String>();
			for (RequiresPermission permission : permissions) {
				
				// 构建数据限制条件SQL
				List<String> parts = new ArrayList<String>();
				for (RequiresPermissionColumn column : permission.value()) {
					if(!StringUtils.isNotBlank(column.perms())) {
						continue;
					}
					switch (column.condition()) {
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
							partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
									.map(perm -> String.format(column.condition().getOperator(), alias, column.column(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
							partSQL.append(" ) ");
							parts.add(partSQL.toString());
						};break;
						case IN:{
							String inPart = Stream.of(StringUtils.split(column.perms(),","))
								.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
							parts.add(String.format(column.condition().getOperator(), alias, column.column(), inPart));
						};break;
						case BITAND_GT:
						case BITAND_GTE:
						case BITAND_LT:
						case BITAND_LTE:
						case BITAND_EQ:{
							StringBuilder partSQL = new StringBuilder();
							partSQL.append(" ( ");
							partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
									.map(perm -> String.format(column.condition().getOperator(), Integer.parseInt(perm), alias, column.column())).collect(Collectors.joining(" OR ")));
							partSQL.append(" ) ");
							parts.add(partSQL.toString());
						};break;
						case EXISTS:
						case NOT_EXISTS:{
							RequiresPermissionForeign foreign = column.foreign();
							StringBuilder partSQL = new StringBuilder();
							// 开始构建关联SQL
							partSQL.append(" SELECT ").append(" fkt.").append(foreign.column());
							partSQL.append(" FROM ").append(foreign.table()).append(" fkt ");
							partSQL.append(" WHERE ").append(" fkt.").append(foreign.column()).append(" = ").append(alias).append(column.column());
							// 构建两个表关联关系条件SQL
							switch (foreign.condition()) {
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
									partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
											.map(perm -> String.format(foreign.condition().getOperator(), "fkt", foreign.column(), StringUtils.quote(perm))).collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								};break;
								case IN:{
									String inPart = Stream.of(StringUtils.split(column.perms(),","))
										.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
									partSQL.append(String.format(foreign.condition().getOperator(), "fkt", foreign.column(), inPart));
								};break;
								case BITAND_GT:
								case BITAND_GTE:
								case BITAND_LT:
								case BITAND_LTE:
								case BITAND_EQ:{
									partSQL.append(" AND ( ");
									partSQL.append(Stream.of(StringUtils.split(column.perms(),","))
											.map(perm -> String.format(foreign.condition().getOperator(), Integer.parseInt(perm), "fkt", foreign.column())).collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								};break;
								default:{};break;
							}
							parts.add(String.format(column.condition().getOperator(), partSQL.toString()));
						};break;
						default:{};break;
					}
					tindex ++;
				}
				// 添加每列的限制条件
				columnParts.add(StringUtils.join(parts, permission.relation().getOperator()));
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
