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
package org.apache.mybatis.dbperms.annotation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public enum Relational {

	AND(" AND ", "且"),
    OR(" OR ","或");
	
	private final String operator;
	private final String placeholder;
	
	Relational(String operator, String placeholder) {
        this.operator = operator;
        this.placeholder = placeholder;
    }

    public static Relational fromString(String operator){
        for (Relational relational : Relational.values()) {
            if(StringUtils.equalsIgnoreCase(StringUtils.trim(relational.operator), StringUtils.trim(operator.toUpperCase())) ){
                return relational;
            }
        }
        throw new RuntimeException("Operator " + operator + " is not supported!");
    }
    
    public Map<String, String> toMap() {
		Map<String, String> driverMap = new HashMap<String, String>();
		driverMap.put("key", this.name());
		driverMap.put("value", this.getPlaceholder());
		return driverMap;
	}
	
	public static List<Map<String, String>> toList() {
		List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
		for (Relational relational : Relational.values()) {
			mapList.add(relational.toMap());
		}
		return mapList;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
}
