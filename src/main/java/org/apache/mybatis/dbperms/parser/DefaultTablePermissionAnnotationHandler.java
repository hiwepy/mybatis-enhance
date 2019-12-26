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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.utils.CollectionUtils;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionColumn;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionForeign;
import org.apache.mybatis.dbperms.annotation.RequiresSpecialPermission;
import org.apache.mybatis.dbperms.utils.PatternFormatUtils;
import org.apache.mybatis.dbperms.utils.StringUtils;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/vindell">vindell</a>
 */
public class DefaultTablePermissionAnnotationHandler implements ITablePermissionAnnotationHandler {

	private String alias = "t0";
	
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresSpecialPermission permission) {
		// 防空判断
		if(null != permission && StringUtils.isNotBlank(permission.sql())) {
			Map<String, String> variables = new HashMap<String, String>();
			// 数据对象表
			variables.put("table", permission.table());
			return PatternFormatUtils.format(permission.sql(), variables);
		}
		return null;
	}
	 
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresPermission permission) {
		// 防空判断
		if(null != permission && ArrayUtils.isNotEmpty(permission.value())) {
			// 动态构建SQL
			List<String> columnParts = this.columnParts(alias, permission);
			if(CollectionUtils.isNotEmpty(columnParts)) {

				StringBuilder builder = new StringBuilder();
				builder.append(" (");
				builder.append(" SELECT ").append(alias).append(".*");
				builder.append(" FROM ").append(permission.table()).append(" ").append(alias);
				builder.append(" WHERE ").append(StringUtils.join(columnParts, permission.relation().toString() ));
				builder.append(" ) ");
				
				return builder.toString();
			}
		}
		
		return null;
	}
	
	List<String> columnParts(String alias, RequiresPermission permission){
		List<String> columnParts = new ArrayList<String>();
		for (RequiresPermissionColumn column : permission.value()) {
			if(null == column.perms()) {
				continue;
			}
			// 数据权限值数组
			String[] permsArr = StringUtils.split(column.perms(),",");
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
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.condition().getOperator(), alias, column.column(), StringUtils.quote(permsArr[0])));
					} else {
						StringBuilder partSQL = new StringBuilder();
						partSQL.append(" ( ");
						partSQL.append(Stream.of(permsArr)
								.map(perm -> String.format(column.condition().getOperator(), alias, column.column(), StringUtils.quote(perm)))
								.collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						columnParts.add(partSQL.toString());
					}
				};break;
				case IN:{
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.condition().getOperator(), alias, column.column(), StringUtils.quote(permsArr[0])));
					} else {
						String inPart = Stream.of(permsArr)
								.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
						columnParts.add(String.format(column.condition().getOperator(), alias, column.column(), inPart));
					}
				};break;
				case BITAND_GT:
				case BITAND_GTE:
				case BITAND_LT:
				case BITAND_LTE:
				case BITAND_EQ:{
					if(permsArr.length == 1) {
						columnParts.add(String.format(column.condition().getOperator(), Integer.parseInt(permsArr[0]), alias, column.column()));
					} else {
						StringBuilder partSQL = new StringBuilder();
						partSQL.append(" ( ");
						partSQL.append(Stream.of(permsArr)
								.map(perm -> String.format(column.condition().getOperator(), Integer.parseInt(perm), alias, column.column()))
								.collect(Collectors.joining(" OR ")));
						partSQL.append(" ) ");
						columnParts.add(partSQL.toString());
					}
				};break;
				case EXISTS:
				case NOT_EXISTS:{
					RequiresPermissionForeign foreign = column.foreign();
					if(null != foreign){
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
								if(permsArr.length == 1) {
									partSQL.append(" AND ").append(String.format(foreign.condition().getOperator(), "fkt", foreign.column(), StringUtils.quote(permsArr[0])));
								} else {	
									partSQL.append(" AND ( ");
									partSQL.append(Stream.of(permsArr)
											.map(perm -> String.format(foreign.condition().getOperator(), "fkt", foreign.column(), StringUtils.quote(perm)))
											.collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								}
							};break;
							case IN:{
								if(permsArr.length == 1) {
									partSQL.append(" AND ").append(String.format(foreign.condition().getOperator(), "fkt", foreign.column(), StringUtils.quote(permsArr[0])));
								} else {	
									String inPart = Stream.of(permsArr)
											.map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
									partSQL.append(" AND ").append(String.format(foreign.condition().getOperator(), "fkt", foreign.column(), inPart));
								}
							};break;
							case BITAND_GT:
							case BITAND_GTE:
							case BITAND_LT:
							case BITAND_LTE:
							case BITAND_EQ:{
								if(permsArr.length == 1) {
									partSQL.append(" AND ").append(String.format(foreign.condition().getOperator(), Integer.parseInt(permsArr[0]), "fkt", foreign.column()));
								} else {	
									partSQL.append(" AND ( ");
									partSQL.append(Stream.of(permsArr)
											.map(perm -> String.format(foreign.condition().getOperator(), Integer.parseInt(perm), "fkt", foreign.column()))
											.collect(Collectors.joining(" OR ")));
									partSQL.append(" ) ");
								}
							};break;
							default:{};break;
						}
						columnParts.add(String.format(column.condition().getOperator(), partSQL.toString()));
					}
				};break;
				default:{};break;
			}
		}
		return columnParts;
	}

}
