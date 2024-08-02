/*
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
package org.apache.ibatis.type;

import java.sql.*;

/**
 * 将字符串格式的时间:<code>yyyy-mm-dd hh:mm:ss[.f...]</code>转换为JDBC能够识别的类型。
 */
public class TimestampTypeHanlder extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        Timestamp timestamp = Timestamp.valueOf(parameter);
        ps.setTimestamp(i, timestamp);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        String dateTime = timestamp.toString();
        dateTime = dateTime.substring(0, dateTime.lastIndexOf("."));
        return dateTime;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        String dateTime = timestamp.toString();
        dateTime = dateTime.substring(0, dateTime.lastIndexOf("."));
        return dateTime;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        String dateTime = timestamp.toString();
        dateTime = dateTime.substring(0, dateTime.lastIndexOf("."));
        return dateTime;
    }

}
