package org.apache.mybatis.enhance.crypto.annotation;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;

import java.lang.annotation.*;

/**
 * 加密字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface EncryptField {

    /**
     * 生成密钥的算法类型,系统支持 sm1、sm4
     */
    String algorithmType();
    /**
     * 模式
     * 加密算法模式，是用来描述加密算法（此处特指分组密码，不包括流密码，）在加密时对明文分组的模式，它代表了不同的分组方式
     */
    Mode mode() default Mode.CBC;

    /**
     * 补码方式：
     * 补码方式是在分组密码中，当明文长度不是分组长度的整数倍时，需要在最后一个分组中填充一些数据使其凑满一个分组的长度。
     */
    Padding padding() default Padding.PKCS5Padding;

    /**
     * 密钥，支持三种密钥长度：128、192、256位
     */
    String key();

    /**
     * 偏移向量，加盐
     */
    String iv() default "";

}
