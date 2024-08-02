package org.apache.mybatis.enhance.annotation;

import java.lang.annotation.*;

/**
 * 国际化主键注解
 * @author hiwepy
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface I18nPrimary {

	String value() default "";

}
