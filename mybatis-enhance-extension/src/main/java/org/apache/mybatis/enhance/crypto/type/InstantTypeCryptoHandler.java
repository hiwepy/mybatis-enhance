/*
package org.apache.mybatis.enhance.crypto.type;

import org.apache.ibatis.type.InstantTypeHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.sql.*;
import java.time.Instant;

public class InstantTypeCryptoHandler extends InstantTypeHandler {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType) throws SQLException {
        AnnotationUtils.getAnnotation(InstantTypeCryptoHandler.class, JdbcType.class);

        ps.setTimestamp(i, Timestamp.from(parameter));
    }

    @Override
    public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return getInstant(timestamp);
    }

    @Override
    public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return getInstant(timestamp);
    }

    @Override
    public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return getInstant(timestamp);
    }

    private static Instant getInstant(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toInstant();
        }
        return null;
    }


}
*/
