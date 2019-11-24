/** 
 * Copyright (C) 2018 Jeebiz (http://jeebiz.net).
 * All Rights Reserved. 
 */
package org.apache.mybatis.dbperms.function;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.ibatis.plugin.meta.MetaStatementHandler;

public class InterceptFunctionFactory {

	protected static ConcurrentMap<String, Function<MetaStatementHandler, Set<String>>> COMPLIED_FUNCTIONS = new ConcurrentHashMap<String, Function<MetaStatementHandler, Set<String>>>();

	public static Function<MetaStatementHandler, Set<String>> getFunction(String statement) {
		return COMPLIED_FUNCTIONS.get(statement);
	}

	public static Function<MetaStatementHandler, Set<String>> setFunction(String statement,
			Function<MetaStatementHandler, Set<String>> perms) {
		return COMPLIED_FUNCTIONS.putIfAbsent(statement, perms);
	}

}
