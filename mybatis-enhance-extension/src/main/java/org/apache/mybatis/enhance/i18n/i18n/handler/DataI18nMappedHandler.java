/***
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
package org.apache.mybatis.enhance.i18n.i18n.handler;


import org.apache.mybatis.enhance.annotation.I18nMapper;
import org.apache.mybatis.enhance.annotation.I18nPrimary;

import java.util.Locale;


public interface DataI18nMappedHandler {

	String getPrimaryName(I18nPrimary i18nPrimary, Object source) throws Exception ;

	DataI18nMapper handle(Locale locale, I18nMapper i18nMapper, String primaryName , Object orginObject, Object i18nObject) throws Exception ;

}
