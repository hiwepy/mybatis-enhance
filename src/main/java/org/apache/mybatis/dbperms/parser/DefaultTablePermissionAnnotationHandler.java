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
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionColumn;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionForeign;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/vindell">vindell</a>
 */
public class DefaultTablePermissionAnnotationHandler implements ITablePermissionAnnotationHandler {

	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresPermission permission) {
		RequiresPermissionColumn[] columns = permission.value();
		if(ArrayUtils.isNotEmpty(columns)) {
			
			String alias = RandomStringUtils.random(3);
			
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(" SELECT ").append(alias).append(".*");
			builder.append(" FROM ").append(permission.table()).append(" ").append(alias);
			// 构建数据限制条件SQL
			List<String> parts = new ArrayList<String>();
			for (RequiresPermissionColumn column : columns) {
				switch (column.condition()) {
					case GT:
					case GTE:
					case LT:
					case LTE:
					case EQ:
					case NE:
					case IN:
					case LIKE:
					case LIKE_LEFT:
					case LIKE_RIGHT:
					case INSTR_GT:
					case INSTR_GTE:
					case INSTR_LT:
					case INSTR_LTE:
					case INSTR_EQ:{
						parts.add(String.format(column.condition().toString(), alias, column.column(), column.perms()));
					};break;
					case BITAND_GT:
					case BITAND_GTE:
					case BITAND_LT:
					case BITAND_LTE:
					case BITAND_EQ:{
						int sum = Stream.of(StringUtils.split(column.perms(),"")).mapToInt(perm->Integer.parseInt(perm)).sum();
						parts.add(String.format(column.condition().toString(), sum, alias, column.column()));
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
							case IN:
							case LIKE:
							case LIKE_LEFT:
							case LIKE_RIGHT:
							case INSTR_GT:
							case INSTR_GTE:
							case INSTR_LT:
							case INSTR_LTE:
							case INSTR_EQ:{
								partSQL.append(String.format(foreign.condition().toString(), "fkt", foreign.column(), column.perms()));
							};break;
							case BITAND_GT:
							case BITAND_GTE:
							case BITAND_LT:
							case BITAND_LTE:
							case BITAND_EQ:{
								int sum = Stream.of(StringUtils.split(column.perms(),"")).mapToInt(perm->Integer.parseInt(perm)).sum();
								partSQL.append(String.format(foreign.condition().toString(), sum, "fkt", foreign.column()));
							};break;
							default:{};break;
						}
						parts.add(String.format(column.condition().toString(), partSQL.toString()));
					};break;
					default:{};break;
				}
			}
			builder.append(" WHERE ").append(StringUtils.join(parts, permission.relation().toString() ));
			builder.append(" )");
			
			return builder.toString();
		}
		
		return permission.table();
	}

}
