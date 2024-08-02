package org.apache.mybatis.enhance.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.mybatis.enhance.annotation.ForeignCondition;

@Getter
@Setter
@ToString
public class DataPermissionForeign {

	/***
	 * 受限表字段关联表之间的关联条件
	 */
	public ForeignCondition condition;
	/***
	 *外关联表名称（实体表名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
	 */
	public String table;
	/***
	 *外关联表字段（实体表字段列名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
	 */
	public String column;

}
