package com.moon.more.excel;

import java.util.function.Function;

/**
 * @author benshaoye
 */
abstract class ProxyBuilder<FROM, R> extends BaseProxy<String> implements Function<FROM, R> {

    protected ProxyBuilder(String key) { super(key); }

    /**
     * build a R
     *
     * @param from 构建源
     *
     * @return 构建成功后的对象
     */
    abstract R build(FROM from);

    /**
     * build a R
     *
     * @param from 构建源
     *
     * @return 构建成功后的对象
     */
    @Override
    public R apply(FROM from) { return build(from); }
}
