package org.apache.mybatis.enhance.crypto.interceptor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.mybatis.enhance.crypto.annotation.EncryptField;
import org.apache.mybatis.enhance.crypto.handler.FieldCryptoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executor;
/**
 * 对update操作进行拦截，对{@link EncryptField}字段进行加密处理；
 * 无论是save方法还是saveBatch方法都会被成功拦截；
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Component
public class EncryptInterceptor implements Interceptor {

    private static final String METHOD = "update";

    @Setter(onMethod_ = {@Autowired})
    private FieldCryptoHandler fieldEncryptUtil;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(!StringUtils.equals(METHOD, invocation.getMethod().getName())) {
            return invocation.proceed();
        }

        // 根据update拦截规则，第0个参数一定是MappedStatement，第1个参数是需要进行判断的参数
        Object param = invocation.getArgs()[1];
        if(Objects.isNull(param)) {
            return invocation.proceed();
        }

        // 加密处理
        fieldEncryptUtil.encrypt(param);

        return invocation.proceed();
    }
}
