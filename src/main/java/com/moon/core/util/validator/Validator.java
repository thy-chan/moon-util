package com.moon.core.util.validator;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 对象多条件验证器：
 *
 * @author benshaoye
 */
public final class Validator<T> extends BaseValidator<T, Validator<T>> {

    public Validator(T value) { this(value, false); }

    public Validator(T value, boolean nullable) { this(value, nullable, null, SEPARATOR, false); }

    Validator(
        T value, boolean nullable, List<String> messages, String separator, boolean immediate
    ) { super(value, nullable, messages, separator, immediate); }

    public static <T> Validator<T> of(T value) { return new Validator<>(value); }

    public static <T> Validator<T> ofNullable(T value) { return new Validator<>(value, true); }

    /**
     * 条件验证
     * <p>
     * 在前置条件匹配的情况下会执行 when 和 end 之间的验证或其他逻辑
     *
     * @param tester          进入条件验证的函数
     * @param scopedValidator 符合验证条件下的验证函数
     *
     * @return 当前 IValidator 对象
     */
    @Override
    public Validator<T> when(
        Predicate<? super T> tester, Consumer<Validator<T>> scopedValidator
    ) { return super.when(tester, scopedValidator); }

    /**
     * 当验证通过时执行处理
     *
     * @param consumer 处理器
     *
     * @return 当前 Validator 对象
     */
    @Override
    public Validator<T> ifValid(Consumer<? super T> consumer) { return super.ifValid(consumer); }

    /**
     * 当验证不通过时执行处理
     *
     * @param consumer 处理器
     *
     * @return 当前 Validator 对象
     */
    @Override
    public Validator<T> ifInvalid(Consumer<? super T> consumer) { return super.ifInvalid(consumer); }

    /**
     * 直接添加一条错误消息
     *
     * @param message 消息内容
     *
     * @return 当前 Validator 实例
     */
    @Override
    public Validator<T> addErrorMessage(String message) { return super.addErrorMessage(message); }

    /**
     * 可用于在后面条件验证前预先设置一部分默认值
     * <p>
     * 只能改变{@link #value}内部属性的值，不能重新设置 value
     *
     * @param consumer 预处理器
     *
     * @return 当前 Validator 实例
     */
    @Override
    public Validator<T> preset(Consumer<? super T> consumer) { return super.preset(consumer); }

    /**
     * 设置是否立即终止，如果设置了即时终止，那么在设置之后第一个验证不通过会立即抛出异常
     * <p>
     * 否则在最后才抛出异常
     *
     * @param immediate 是否即时终止
     *
     * @return 当前 Validator 实例
     */
    @Override
    public Validator<T> setImmediate(boolean immediate) { return super.setImmediate(immediate); }

    /**
     * 设置错误信息分隔符，不能为 null
     *
     * @param separator 错误消息分割符
     *
     * @return 当前 Validator 实例
     */
    @Override
    public Validator<T> setSeparator(String separator) { return super.setSeparator(separator); }

    /**
     * 要求符合条件
     *
     * @param tester  断言函数
     * @param message 错误消息
     *
     * @return 当前 Validator 对象
     */
    @Override
    public Validator<T> require(Predicate<? super T> tester, String message) { return super.require(tester, message); }

    /**
     * 要求符合指定验证规则
     *
     * @param tester 验证函数
     *
     * @return 当前 IValidator 对象
     */
    @Override
    public Validator<T> require(Predicate<? super T> tester) { return super.require(tester); }
}
