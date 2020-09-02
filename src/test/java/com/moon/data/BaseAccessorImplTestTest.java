package com.moon.data;

import com.moon.data.jpa.domain.BaseAuditRecordEntity;
import com.moon.data.service.BaseServiceImpl;
import com.moon.data.service.DataServiceImpl;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author moonsky
 */
class BaseAccessorImplTestTest {

    @Data
    public static class AccountEntity extends BaseAuditRecordEntity {

        private String name;
    }

    public static class AccountBaseServe extends BaseServiceImpl<AccountEntity> {}
    public static class AccountDataServe extends DataServiceImpl<AccountEntity> {}

    @Test
    void testDeduceDomainClass() throws Exception {
        Class testingClass = AccountDataServe.class;
        Type accountSuperclass = testingClass.getGenericSuperclass();
        if (accountSuperclass instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) accountSuperclass;
            System.out.println(type.getRawType());
            System.out.println(Arrays.toString(type.getActualTypeArguments()));
        }
    }
}