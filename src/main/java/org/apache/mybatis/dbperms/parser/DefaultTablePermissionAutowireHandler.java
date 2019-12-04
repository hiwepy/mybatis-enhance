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
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.mybatis.dbperms.interceptor.DataPermission;
import org.apache.mybatis.dbperms.interceptor.DataPermissionColumn;
import org.apache.mybatis.dbperms.interceptor.DataPermissionForeign;

/**
 * 根据注解的权限信息组装权限语句
 * @author <a href="https://github.com/vindell">vindell</a>
 */
public class DefaultTablePermissionAutowireHandler implements ITablePermissionAutowireHandler {

	private BiFunction<MetaStatementHandler, String, Optional<DataPermission>> permissionProvider;
	
	public DefaultTablePermissionAutowireHandler(BiFunction<MetaStatementHandler, String, Optional<DataPermission>> permissionProvider) {
		this.permissionProvider = permissionProvider;
	}
	
	@Override
	public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String tableName) {
		
		Optional<DataPermission> permission = permissionProvider.apply(metaHandler, tableName);
		String parsedSql = tableName; 
		if(null != permission && permission.isPresent()) {
				
			String alias = RandomStringUtils.random(3);
			
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append("SELECT ").append(alias).append(".*");
			builder.append("FROM ").append(permission.get().getTable()).append(" ").append(alias);
			// 构建数据限制条件SQL
			List<String> parts = new ArrayList<String>();
			for (DataPermissionColumn column : permission.get().getColumns()) {
				switch (column.getCondition()) {
					case GT:
					case GTE:
					case LT:
					case LTE:
					case EQ:
					case NE:
					case IN:
					case LIKE:
					case LIKE_LEFT:
					case LIKE_RIGHT:{
						parts.add(String.format(column.getCondition().toString(), alias, column.getColumn(), column.getPerms()));
					};break;
					case BITAND_GT:
					case BITAND_GTE:
					case BITAND_LT:
					case BITAND_LTE:
					case BITAND_EQ:{
						int sum = Stream.of(StringUtils.split(column.getPerms(),"")).mapToInt(perm -> Integer.parseInt(perm)).sum();
						parts.add(String.format(column.getCondition().toString(), sum, alias, column.getColumn()));
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
							case IN:
							case LIKE:
							case LIKE_LEFT:
							case LIKE_RIGHT:{
								partSQL.append(String.format(foreign.getCondition().toString(), "fkt", foreign.getColumn(), column.getPerms()));
							};break;
							case BITAND_GT:
							case BITAND_GTE:
							case BITAND_LT:
							case BITAND_LTE:
							case BITAND_EQ:{
								int sum = Stream.of(StringUtils.split(column.getPerms(),"")).mapToInt(perm->Integer.parseInt(perm)).sum();
								partSQL.append(String.format(foreign.getCondition().toString(), sum, "fkt", foreign.getColumn()));
							};break;
							default:{};break;
						}
						parts.add(String.format(column.getCondition().toString(), partSQL.toString()));
					};break;
					default:{};break;
				}
			}
			builder.append("WHERE ").append(StringUtils.join(parts, permission.get().getRelation().toString() ));
			builder.append(")");
			
			parsedSql = builder.toString();
			
		}
		
		return parsedSql;
	}

}
