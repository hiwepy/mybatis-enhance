package org.apache.mybatis.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataSpecialPermission {
	
	/**
	 *受限表名称（实体表名称）
	 */
	private String table;
	/**
	 * 受限表字段名称（实体表字段列名称）
	 */
	private String column;
	/**
	 *受限表转换后的SQL(直接使用SQL进行替换，减少性能消耗)
	 */
	private String sql;
	/**
	 * 数据权限可用状态:（0:不可用|1：可用）
	 */
	private int status;
	/**
	 * 数据权限排序
	 */
	private int order;
	
}
