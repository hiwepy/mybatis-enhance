/**
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
package org.apache.mybatis.dbperms;

import org.apache.mybatis.dbperms.annotation.Relational;
import org.apache.mybatis.dbperms.annotation.RequiresPermission;
import org.apache.mybatis.dbperms.annotation.RequiresPermissionItem;
import org.apache.mybatis.dbperms.annotation.RequiresPermissions;

public class Test {
	
	/**
	 * 自动注入方式
	 */
	@RequiresPermissions
	public void test1() {
		
	}
	
	/**
	 * 手动定义方式
	 */
	@RequiresPermissions(
		autowire = false,
		value = {
			@RequiresPermission(table = "XXX_XXB", value = {
				@RequiresPermissionItem(column = "xxxx", perms = "id=xx,xx=sss"),
				@RequiresPermissionItem(column = "xxxx", perms = "id=xx,xx=sss")
			}, relation =  Relational.AND),
			@RequiresPermission(table = "YYY_XXB", value = {
				@RequiresPermissionItem(column = "xxxx", perms = "id=xx,xx=sss"),
				@RequiresPermissionItem(column = "xxxx", perms = "id=xx,xx=sss")
			}, relation =  Relational.OR)
		}
	)
	public void test2() {
		
	}
	
}
