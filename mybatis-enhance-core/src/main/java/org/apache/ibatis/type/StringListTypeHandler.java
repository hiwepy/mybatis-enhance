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

import org.apache.commons.lang3.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, List<String> list, JdbcType jdbcType)
			throws SQLException {
		if(list != null && !list.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (String s : list) {
				sb.append(s).append(",");
			}
			ps.setString(i, sb.toString().substring(0, sb.toString().length() - 1));
		} else {
			ps.setString(i, "");
		}
	}

	@Override
	public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String rtString = rs.getString(columnName);
		if(StringUtils.isNotBlank(rtString)) {
			return Arrays.asList(rtString.split(","));
		}
		return null;
	}

	@Override
	public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String rtString = rs.getString(columnIndex);
		if(StringUtils.isNotBlank(rtString)) {
			return Arrays.asList(rtString.split(","));
		}
		return null;
	}

	@Override
	public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String rtString = cs.getString(columnIndex);
		if(StringUtils.isNotBlank(rtString)) {
			return Arrays.asList(rtString.split(","));
		}
		return null;
	}

}
