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
package org.apache.mybatis.enhance.annotation;

import java.util.*;

public enum ForeignCondition {

	/***
     * 大于
     */
	GT(" %s.%s > %s ", "[数据字段]大于[数据项]"),
	/***
     * 大于或等于
     */
	GTE(" %s.%s >= %s ", "[数据字段]大于或等于[数据项]"),
    /***
     * 小于
     */
    LT(" %s.%s < %s ", "[数据字段]小于[数据项]"),
    /***
     * 小于或等于
     */
    LTE(" %s.%s <= %s ", "[数据字段]小于或等于[数据项]"),
    /***
     * 等于
     */
    EQ(" %s.%s = %s ", "[数据字段]等于[数据项]"),
    /***
     * 不等于
     */
    NE(" %s.%s != %s ", "[数据字段]不等于[数据项]"),
    /***
     * 在指定范围 in ()
     */
    IN(" %s.%s IN (%s) ", "[数据字段]在[数据项]范围内"),
    /***
     * % 两边 %
     */
    LIKE(" %s.%s LIKE CONCAT('%%', %s ,'%%') ", "[数据字段]包含指定[数据项]"),
    /***
     * % 左
     */
    LIKE_LEFT(" %s.%s LIKE CONCAT('%%', %s) ", "[数据字段]以[数据项]开始"),
    /***
     * 右 %
     */
    LIKE_RIGHT(" %s.%s LIKE CONCAT(%s,'%%') ", "[数据字段]以[数据项]结束"),

    BITAND_GT(" bitand(%s, to_number(%s.%s)) > 0", "[数据字段]与[数据项]按位运行大于0：bitand(数据项, 数据字段) > 0"),
    BITAND_GTE(" bitand(%s, to_number(%s.%s)) >= 0", "[数据字段]与[数据项]按位运行大于或等于0：bitand(数据项, 数据字段) >= 0"),
    BITAND_LT(" bitand(%s, to_number(%s.%s)) < 0", "[数据字段]与[数据项]按位运行小于0：bitand(数据项, 数据字段) < 0"),
    BITAND_LTE(" bitand(%s, to_number(%s.%s)) <= 0", "[数据字段]与[数据项]按位运行小于或等于0：bitand(数据项, 数据字段) <= 0"),
    BITAND_EQ(" bitand(%s, to_number(%s.%s)) = 0", "[数据字段]与[数据项]按位运行等于0：bitand(数据项, 数据字段) => 0");

	private final String operator;
	private final String placeholder;

	ForeignCondition(String operator, String placeholder) {
        this.operator = operator;
        this.placeholder = placeholder;
    }

    public static ForeignCondition fromString(String operator){
        for (ForeignCondition condition : ForeignCondition.values()) {
            if(condition.operator.equals(operator.toUpperCase()) ){
                return condition;
            }
        }
        throw new NoSuchElementException("Filter operator " + operator + " is not supported!");
    }

	public Map<String, String> toMap() {
		Map<String, String> driverMap = new HashMap<String, String>();
		driverMap.put("key", this.name());
		driverMap.put("value", this.getPlaceholder());
		return driverMap;
	}

	public static List<Map<String, String>> toList() {
		List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
		for (ForeignCondition condition : ForeignCondition.values()) {
			mapList.add(condition.toMap());
		}
		return mapList;
	}

	public String getOperator() {
		return operator;
	}

	public String getPlaceholder() {
		return placeholder;
	}

}
