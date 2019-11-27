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

public enum ForeignCondition {
	
	/**
     * 大于
     */
	GT(" %s.%s > %s "),
	/**
     * 大于或等于
     */
	GTE(" %s.%s >= %s "),
    /**
     * 小于
     */
    LT(" %s.%s < %s "),
    /**
     * 小于或等于
     */
    LTE(" %s.%s <= %s "),
    /**
     * 等于
     */
    EQ(" %s.%s = %s "),
    /**
     * 不等于
     */
    NE(" %s.%s != %s "),
    /**
     * 在指定范围 in ()
     */
    IN(" %s.%s IN (%s) "),
    /**
     * % 两边 %
     */
    LIKE(" %s.%s LIKE CONCAT('%%', %s ,'%%') "),
    /**
     * % 左
     */
    LIKE_LEFT(" %s.%s LIKE CONCAT('%%', %s) "),
    /**
     * 右 %
     */
    LIKE_RIGHT(" %s.%s LIKE CONCAT(%s,'%%') "),
	
    BITAND_GT(" bitand(%s, to_number(%s.%s)) > 0"),
    BITAND_GTE(" bitand(%s, to_number(%s.%s)) >= 0"),
    BITAND_LT(" bitand(%s, to_number(%s.%s)) < 0"),
    BITAND_LTE(" bitand(%s, to_number(%s.%s)) <= 0"),
    BITAND_EQ(" bitand(%s, to_number(%s.%s)) = 0"),
    
    INSTR_GT(" instr(%s, %s) > %s "),
    INSTR_GTE(" instr(%s, %s) >= %s "),
    INSTR_LT(" instr(%s, %s) < %s "),
    INSTR_LTE(" instr(%s, %s) <= %s "),
    INSTR_EQ(" instr(%s, %s) = %s ");
	
	private final String operator;

	ForeignCondition(String operator) {
        this.operator = operator;
    }

    public static ForeignCondition fromString(String operator){
        for (ForeignCondition relational : ForeignCondition.values()) {
            if(relational.operator.equals(operator.toUpperCase()) ){
                return relational;
            }
        }
        throw new RuntimeException("Filter operator " + operator + " is not supported!");
    }

    @Override
    public String toString() {
        return operator;
    }
	
}
