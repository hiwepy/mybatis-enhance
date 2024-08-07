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

import com.alibaba.fastjson2.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JSONArrayTypeHandler extends BaseTypeHandler<JSONArray> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.toJSONString());
	}

	@Override
	public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String rtString = rs.getString(columnName);
		if(StringUtils.isNotBlank(rtString)) {
			return JSONArray.parseArray(rtString);
		}
		return null;
	}

	@Override
	public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String rtString = rs.getString(columnIndex);
		if(StringUtils.isNotBlank(rtString)) {
			return JSONArray.parseArray(rtString);
		}
		return null;
	}

	@Override
	public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String rtString = cs.getString(columnIndex);
		if(StringUtils.isNotBlank(rtString)) {
			return JSONArray.parseArray(rtString);
		}
		return null;
	}

}
