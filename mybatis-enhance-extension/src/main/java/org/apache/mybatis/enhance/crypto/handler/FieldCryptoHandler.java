package org.apache.mybatis.enhance.crypto.handler;

public interface FieldCryptoHandler {

    /**
     * 字段加密
     * @param value 待加密字段的值
     * @return T 加密后的字段值
     * @param <T> 字段类型
     */
    <T> T encrypt(T value);

    /**
     * 字段解密
     * @param value 待解密字段的值
     * @return T 解密后的字段值
     * @param <T> 字段类型
     */
    <T> T decrypt(T value);

}
