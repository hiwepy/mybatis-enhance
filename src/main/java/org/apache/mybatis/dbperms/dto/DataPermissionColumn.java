package org.apache.mybatis.dbperms.dto;

import org.apache.mybatis.dbperms.annotation.Condition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataPermissionColumn {
	
	/**
	 * 受限表字段名称（实体表字段列名称）
	 */
	private String column;
	/**
	 * 受限表字段与限制条件之间的关联条件
	 */
	public Condition condition;
	/**
	 *外关联表名称（实体表名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
	 */
	public DataPermissionForeign foreign;
	/**
	 * 受限表字段限制条件
	 */
	private String perms;
	/**
	 * 受限表字段可用状态:（0:不可用|1：可用）
	 */
	private int status;
	/**
	 * 受限表字段排序
	 */
	private int order;
	
}
