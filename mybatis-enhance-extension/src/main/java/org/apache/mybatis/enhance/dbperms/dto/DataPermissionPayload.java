/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package org.apache.mybatis.enhance.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.mybatis.enhance.annotation.Relational;

import java.util.List;

@Getter
@Setter
@ToString
public class DataPermissionPayload {

	String code;

	/***
	 * 普通数据权限
	 */
	List<DataPermission> permissions;

	/***
	 * 普通数据权限与特殊数据权限的关系 and/or
	 */
	private Relational relation = Relational.AND;

	/***
	 * 特殊数据权限
	 */
	List<DataSpecialPermission> specialPermissions;

}
