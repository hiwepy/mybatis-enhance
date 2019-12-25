package org.apache.mybatis.dbperms.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.mybatis.dbperms.annotation.Relational;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataPermission {
	
	/**
	 *受限表名称（实体表名称）
	 */
	private String table;
	/**
	 *受限表转换后的SQL(直接使用SQL进行替换，减少性能消耗)
	 */
	private String sql;
	/**
	 * 数据权限项关系 and/or
	 */
	private Relational relation = Relational.AND;
	/**
	 * 数据权限项集合
	 */
	private List<DataPermissionColumn> columns = new ArrayList<>();
	/**
	 * 数据权限可用状态:（0:不可用|1：可用）
	 */
	private int status;
	/**
	 * 数据权限排序
	 */
	private int order;
	
}
