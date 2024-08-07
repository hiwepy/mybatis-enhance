package org.apache.mybatis.enhance.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface I18nSwitch {

	public abstract I18nColumn[] value() default {};

}
