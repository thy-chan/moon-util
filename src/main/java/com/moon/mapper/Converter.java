package com.moon.mapper;

/**
 * @author benshaoye
 */
public interface Converter<THIS, THAT> {

    /**
     * 将{@code thisObject}暗属性映射到{@code THAT}对象
     * <p>
     * 【注】此方法对{@code thisObject}和{@code thatObject}没有 null 判断
     *
     * <pre>
     * public THAT convert(THIS thisObject, THAT thatObject) {
     *     if (thisObject == null) {
     *         return thatObject;
     *     }
     *     // do mapping properties
     *     return thatObject;
     * }
     * </pre>
     *
     * @param thisObject 数据源对象
     * @param thatObject 目标对象
     *
     * @return 返回目标对象
     */
    THAT unsafeConvert(THIS thisObject, THAT thatObject);

    /**
     * 将{@code thisObject}暗属性映射到{@code THAT}对象
     *
     * <pre>
     * public THAT convert(THIS thisObject, THAT thatObject) {
     *     if (thisObject == null) {
     *         return thatObject;
     *     }
     *     // do mapping properties
     *     return thatObject;
     * }
     * </pre>
     *
     * @param thisObject 数据源对象
     * @param thatObject 目标对象
     *
     * @return 返回目标对象
     */
    default THAT convert(THIS thisObject, THAT thatObject) {
        return thisObject == null || thatObject == null ? thatObject : unsafeConvert(thisObject, thatObject);
    }

    /**
     * 将{@code thisObject}转换成{@code THAT}对象
     *
     * <pre>
     * public THAT convert(THIS thisObject) {
     *     if (thisObject == null) {
     *         return null;
     *     }
     *     return convert(thisObject, new [THAT]());
     * }
     * </pre>
     *
     * @param thisObject 数据源对象
     *
     * @return 返回目标对象
     */
    THAT convert(THIS thisObject);
}
