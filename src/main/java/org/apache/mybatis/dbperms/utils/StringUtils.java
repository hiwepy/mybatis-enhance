/*
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
package org.apache.mybatis.dbperms.utils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	/**
	 * Quote the given {@code String} with single quotes.
	 * @param str the input {@code String} (e.g. "myString")
	 * @return the quoted {@code String} (e.g. "'myString'"),
	 * or {@code null} if the input was {@code null}
	 */
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}

	/**
	 * Turn the given Object into a {@code String} with single quotes
	 * if it is a {@code String}; keeping the Object as-is else.
	 * @param obj the input Object (e.g. "myString")
	 * @return the quoted {@code String} (e.g. "'myString'"),
	 * or the input object as-is if not a {@code String}
	 */
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}
	
}
