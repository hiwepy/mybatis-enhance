package org.apache.mybatis.enhance.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

	public abstract boolean autowire() default true;

	public abstract RequiresPermission[] value() default {};

	public abstract RequiresSpecialPermission[] special() default {};

}
