package com.moon.accessor.meta;

import com.moon.accessor.Condition;

/**
 * @author benshaoye
 */
public interface Field<T, R, TB extends Table<R>> {

    /**
     * 领域模型类
     *
     * @return 与数据表对应的实体类
     */
    Class<R> getDomainClass();

    /**
     * 返回字段数据类型
     *
     * @return 字段 java 类型
     */
    Class<T> getPropertyType();

    /**
     * 实体字段名
     *
     * @return 实体字段名
     */
    String getPropertyName();

    /**
     * 获取数据库对应的列名
     *
     * @return 列名
     */
    String getColumnName();

    /**
     * 返回所属数据表
     *
     * @return 数据表描述
     */
    TB getTable();

    /*
     * 以下是条件查询
     */

    /**
     * 字符串以 xxx 开始，这里用{@link Object}是为了兼容别的数据类型，
     * 内部处理通过{@link String#valueOf(Object)}，最终得到：
     * <pre>
     *     field LIKE 'text%'
     * </pre>
     *
     * @param value 期望字符串
     *
     * @return where 条件
     */
    default Condition startsWith(Object value) {
        return null;
    }

    /**
     * 字符串以 xxx 开始，这里用{@link Object}是为了兼容别的数据类型，
     * 内部处理通过{@link String#valueOf(Object)}，最终得到：
     * <pre>
     *     field LIKE '%text'
     * </pre>
     *
     * @param value 期望字符串
     *
     * @return where 条件
     */
    default Condition endsWith(Object value) {
        return null;
    }

    /**
     * 字符串以 xxx 开始，这里用{@link Object}是为了兼容别的数据类型，
     * 内部处理通过{@link String#valueOf(Object)}，最终得到：
     * <pre>
     *     field LIKE '%text%'
     * </pre>
     *
     * @param value 期望字符串
     *
     * @return where 条件
     */
    default Condition contains(Object value) {
        return null;
    }

    /**
     * 字符串以 xxx 开始，这里用{@link Object}是为了兼容别的数据类型，
     * 内部处理通过{@link String#valueOf(Object)}，最终得到：
     * <pre>
     *     field LIKE 'text'
     * </pre>
     * <p>
     * 注意与{@link #startsWith(Object)}、{@link #endsWith(Object)}、{@link #contains(Object)}
     * 的区别，这里不会主动添加任何匹配符号（如：%、_)等，只是单纯的转换成字符串
     * <p>
     * 这是满足更多自定义条件设计的
     *
     * @param value 期望字符串
     *
     * @return where 条件
     */
    default Condition like(Object value) {
        return null;
    }

    /**
     * 小于某个值
     *
     * @param value 期望字段最大值（不包含）
     *
     * @return 条件子句
     */
    default Condition lt(T value) {
        return null;
    }

    /**
     * 大于某个值
     *
     * @param value 期望字段最小值（不包含）
     *
     * @return 条件子句
     */
    default Condition gt(T value) {
        return null;
    }

    /**
     * 小于某个值
     *
     * @param value 期望字段最大值（包含）
     *
     * @return 条件子句
     */
    default Condition le(T value) {
        return null;
    }

    /**
     * 大于某个值
     *
     * @param value 期望字段最小值（不包含）
     *
     * @return 条件子句
     */
    default Condition ge(T value) {
        return null;
    }

    /**
     * 等于某个值
     *
     * @param value 期望的字段值
     *
     * @return 条件子句
     *
     * @see #is(Object) 等价
     */
    default Condition eq(T value) {
        return null;
    }

    /**
     * 等于某个值
     *
     * @param value 期望的字段值
     *
     * @return 条件子句
     *
     * @see #eq(Object) 等价
     */
    default Condition is(T value) {
        return null;
    }

    /**
     * 等不于某个值
     *
     * @param value 不期望的字段值
     *
     * @return 条件子句
     */
    default Condition ne(T value) {
        return null;
    }

    /**
     * 字段是否在指定列表中
     *
     * @param values 期望值列表
     *
     * @return 条件子句
     *
     * @see #in(Iterable) 等价
     */
    default Condition any(T... values) {
        return null;
    }

    /**
     * 字段是否不在指定列表中
     *
     * @param values 字段值不期望的值的列表
     *
     * @return 条件子句
     *
     * @see #notIn(Iterable) 等价
     */
    default Condition notAny(T... values) {
        return null;
    }

    /**
     * 字段是否在指定列表中
     *
     * @param values 期望值列表
     *
     * @return 条件子句
     */
    default Condition in(Iterable<T> values) {
        return null;
    }

    /**
     * 字段是否不在指定列表中
     *
     * @param values 字段值不期望的值的列表
     *
     * @return 条件子句
     */
    default Condition notIn(Iterable<T> values) {
        return null;
    }

    /**
     * 期望字段值为 null
     *
     * @return 条件
     */
    default Condition isNull() {
        return null;
    }

    /**
     * 期望字段值不为 null
     *
     * @return 条件
     */
    default Condition notNull() {
        return null;
    }

    /**
     * 期望字段值不为 null
     *
     * @return 条件
     */
    default Condition isNotNull() {
        return notNull();
    }
}