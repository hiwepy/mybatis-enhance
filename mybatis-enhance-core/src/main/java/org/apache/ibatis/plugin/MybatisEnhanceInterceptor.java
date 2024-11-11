package org.apache.ibatis.plugin;

import org.apache.ibatis.binding.MetaParameterHandler;
import org.apache.ibatis.binding.MetaResultSetHandler;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.meta.MetaExecutor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.Properties;

@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
    @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class MybatisEnhanceInterceptor extends AbstractInterceptor {

    @Override
    public Object doExecutorIntercept(Invocation invocation, Executor executorProxy, MetaExecutor metaExecutor) throws Throwable {
        return null;
    }

    @Override
    public Object doParameterIntercept(Invocation invocation, ParameterHandler parameterHandler, MetaParameterHandler metaParameterHandler) throws Throwable {
        return null;
    }

    @Override
    public Object doStatementIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) throws Throwable {
        return null;
    }

    @Override
    public Object doResultSetIntercept(Invocation invocation, ResultSetHandler resultSetHandler, MetaResultSetHandler metaResultSetHandler) throws Throwable {
        return null;
    }

    @Override
    public void doDestroyIntercept(Invocation invocation) throws Throwable {

    }

    @Override
    public void setInterceptProperties(Properties properties) {

    }
}
