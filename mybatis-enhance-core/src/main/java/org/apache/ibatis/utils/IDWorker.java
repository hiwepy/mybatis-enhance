/**
 * Copyright (c) 2011-2014, hubin (jobob@qq.com).
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
package org.apache.ibatis.utils;

import org.apache.ibatis.utils.uid.Sequence;

import java.util.UUID;

/*
 * <p>
 * 高效GUID产生算法(sequence),基于Snowflake实现64位自增ID算法。 <br>
 * 优化开源项目 http://git.oschina.net/yu120/sequence
 * </p>
 *
 * @author hubin
 * @Date 2016-08-01
 */
public class IDWorker {

	/*
	 * 主机和进程的机器码
	 */
	private static Sequence worker = new Sequence();

	public static long getId() {
		return worker.nextId();
	}

	/*
	 * <p>
	 * 获取去掉"-" UUID
	 * </p>
	 */
	public static synchronized String get32UUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

}
