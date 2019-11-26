package org.apache.mybatis.dbperms.interceptor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataPermissionItemVo {
	
	/**
	 * 受限表字段名称（实体表字段列名称）
	 */
	private String column;
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
