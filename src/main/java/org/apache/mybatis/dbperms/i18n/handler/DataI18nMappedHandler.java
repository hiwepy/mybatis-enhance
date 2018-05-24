/**
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
package org.apache.mybatis.dbperms.i18n.handler;

import java.util.Locale;

import org.apache.mybatis.dbperms.annotation.PermMapper;
import org.apache.mybatis.dbperms.annotation.PermPrimary;


public interface DataI18nMappedHandler {

	String getPrimaryName(PermPrimary i18nPrimary, Object source) throws Exception ;
	
	DataI18nMapper handle(Locale locale,PermMapper i18nMapper, String primaryName , Object orginObject, Object i18nObject) throws Exception ;
	
}
