package org.apache.mybatis.dbperms.interceptor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataPermissionVo {
	
	/**
	 *受限表名称（实体表名称）
	 */
	private String table;
	/**
	 * 数据权限项集合
	 */
	private List<DataPermissionItemVo> items = new ArrayList<>();
	/**
	 * 数据权限可用状态:（0:不可用|1：可用）
	 */
	private int status;
	/**
	 * 数据权限排序
	 */
	private int order;
	
}
