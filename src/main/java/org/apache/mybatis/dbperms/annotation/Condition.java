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

public enum Condition {
	
	GTE(">="),
    LTE("<="),
    
    /**
     * 等于
     */
    EQUAL("%s=#{%s}"),
    /**
     * 不等于
     */
    NOT_EQUAL("%s&lt;&gt;#{%s}"),
    /**
     * Type safe equals
     */
    TSEQ("==="),
    NE("!="),
    
    BITAND_EQ(" bitand(to_number(%s), %s) = %s"),
    BITAND_GT(" bitand(to_number(%s), %s) > %s"),
    BITAND_LT(" bitand(to_number(%s), %s) < %s"),
    BITAND_GTE(" bitand(to_number(%s), %s) >= %s"),
    BITAND_LTE(" bitand(to_number(%s), %s) <= %s"),
    
    INSTR_GT(" instr(%s, #{%s}) > 0"),
    
    /**
     * Type safe not equals
     */
    TSNE("!=="),
    LT("<"),
    GT(">"),
    IN("IN"),
    
    /**
     * % 两边 %
     */
    LIKE("%s LIKE CONCAT('%%',#{%s},'%%')"),
    /**
     * % 左
     */
    LIKE_LEFT("%s LIKE CONCAT('%%',#{%s})"),
    /**
     * 右 %
     */
    LIKE_RIGHT("%s LIKE CONCAT(#{%s},'%%')");
	
	private final String operator;

	Condition(String operator) {
        this.operator = operator;
    }

    public static Condition fromString(String operator){
        for (Condition relational : Condition.values()) {
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
