package com.moon.spring.data.jpa.start;

import com.moon.data.RecordConst;
import com.moon.data.annotation.RecordCacheNamespace;
import com.moon.spring.data.jpa.factory.AbstractRepositoryImpl;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * 开启 Record 缓存支持
 * <p>
 * 基于实体、ID进行全局缓存
 *
 * @author moonsky
 * @see AbstractRepositoryImpl#findFromCacheById(Object, Function, Cache)
 * @see RecordCacheNamespace 缓存命名空间
 */
@SuppressWarnings("all")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JpaRecordCacheRegistrar.class)
public @interface EnableJpaRecordCache {

    String cacheManagerRef() default "cacheManager";

    String group() default RecordConst.CACHE_GROUP;
}
