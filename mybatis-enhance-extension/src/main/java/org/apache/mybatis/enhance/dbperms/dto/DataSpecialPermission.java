package org.apache.mybatis.enhance.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataSpecialPermission extends DataPermission {

	/***
	 * 受限表字段名称（实体表字段列名称）
	 */
	private String column;

}
