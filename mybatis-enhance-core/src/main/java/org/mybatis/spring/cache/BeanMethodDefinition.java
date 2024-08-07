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
package org.mybatis.spring.cache;

import org.apache.ibatis.utils.ObjectUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class BeanMethodDefinition {

	protected BeanDefinition beanDefinition;
	protected String beanName;
	protected String[] aliases;
	protected Class<?> beanClass;
	protected String beanClassName;
	protected static final ConcurrentMap<String , Method> COMPLIED_METHODS = new ConcurrentHashMap<String , Method>();

	public BeanMethodDefinition(String beanName, String[] aliases, BeanDefinition beanDefinition, Class<?> beanClass) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
		this.beanClass = beanClass;
		this.beanClassName = beanClass.getName();
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String getBeanName() {
		return beanName;
	}

	public String[] getAliases() {
		return aliases;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getBeanClassName() {
		return beanClassName;
	}

	public Method getMethod(String methodName, Class<?>[] paramTypes) {
		StringBuilder builder = new StringBuilder(getBeanClassName()).append(".").append(methodName);
		if(!ObjectUtils.isEmpty(paramTypes)){
			builder.append("[");
			for (Class<?> paramType : paramTypes) {
				builder.append(".").append(paramType.getName());
			}
			builder.append("]");
		}
		String uid = DigestUtils.md5DigestAsHex(builder.toString().getBytes());
		Method ret = COMPLIED_METHODS.get(uid);
		if (ret != null) {
			return ret;
		}
		synchronized (beanClass) {

			// 查找对应参数类型方法
			Class<?> searchType = beanClass;
			while (searchType != null) {
				Method[] methods = (searchType.isInterface() ? searchType.getMethods() : ReflectionUtils.getAllDeclaredMethods(searchType));
				for (Method method : methods) {
					if (methodName.equals(method.getName()) && (ObjectUtils.isEmpty(paramTypes) || equals(paramTypes, method.getParameterTypes()))) {
						ret = method;
						Method existing = COMPLIED_METHODS.putIfAbsent(uid, ret);
						if (existing != null) {
							ret = existing;
						}
						return ret;
					}
				}
				searchType = searchType.getSuperclass();
			}

		}

		return ret;
	}

	public boolean equals(Class<?>[] a, Class<?>[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
        	Class<?> class1 = a[i];
        	Class<?> class2 = a2[i];

            if (!(class1==null ? class2==null : (class1.equals(class2) || class2.isAssignableFrom(class1)))){
            	 return false;
            }

        }

        return true;
    }



}
