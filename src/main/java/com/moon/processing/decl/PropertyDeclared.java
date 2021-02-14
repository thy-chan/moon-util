package com.moon.processing.decl;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;

/**
 * @author benshaoye
 */
public class PropertyDeclared {

    private final TypeElement thisElement;

    private final String name;

    private final Map<String, GenericDeclared> thisGenericMap;

    private FieldDeclared fieldDeclared;
    /**
     * 由于 java 方法重载规则， getter 方法只有一个，所以不考虑重载的情况
     * <p>
     * 暂时也不考虑 getXxx(int) 的情况，根据个人经验，从未遇到过这种情况
     * <p>
     * 所以如果实际应用中即使真的出现 getXxx(int) 的 getter，请考虑修改成其他方案
     */
    private MethodDeclared getter;
    /**
     * 从{@link #typedSetterMap}最终选用的 setter 方法
     *
     * <pre>
     * 1. 如果存在 getter，setter 参数类型与 getter 返回值类型一致
     * 2. 若不存在 getter，存在字段，setter 参数类型与字段类型一致
     * 3. 若没有与字段类型一致的 setter 方法就按照一定规则取一个默认的 setter 方法
     * </pre>
     */
    private MethodDeclared setter;
    /**
     * setter 方法，要求 setter 方法参数类型和 getter 方法返回值类型一致
     * <p>
     * 若没有 getter 方法，且存在 setter 重载时，setter 方法取用优先规则为:
     * 1. 与字段声明类型一致
     * 2. 按特定顺序排序后的第一个方法（这种排序规则受 jdk 版本可能受 jvm 版本的影响，所以这里自定义顺序）
     */
    private Map<String, MethodDeclared> typedSetterMap;

    public PropertyDeclared(TypeElement thisElement, String name, Map<String, GenericDeclared> thisGenericMap) {
        this.thisGenericMap = thisGenericMap;
        this.thisElement = thisElement;
        this.name = name;
    }

    public void withFieldDeclared(VariableElement fieldElement) {
        TypeElement enclosingElement = ((TypeElement) fieldElement.getEnclosingElement());
        withFieldDeclared(new FieldDeclared(thisElement, enclosingElement, fieldElement, thisGenericMap));
    }

    public PropertyDeclared withFieldDeclared(FieldDeclared fieldDeclared) {
        this.fieldDeclared = fieldDeclared;
        return this;
    }

    public PropertyDeclared withGetterMethodDeclared(ExecutableElement getterElement) {
        TypeElement enclosingElement = ((TypeElement) getterElement.getEnclosingElement());
        new MethodDeclared(thisElement, enclosingElement, getterElement, thisGenericMap);
        return this;
    }

    public PropertyDeclared withSetterMethodDeclared(
        ExecutableElement setterElement, String declaredType, String actualType
    ) {
        TypeElement enclosingElement = ((TypeElement) setterElement.getEnclosingElement());
        new MethodDeclared(thisElement, enclosingElement, setterElement, thisGenericMap);
        return this;
    }

    public boolean isWriteable(String setterActualClass) {
        return typedSetterMap.containsKey(setterActualClass);
    }

    public boolean isWriteable() { return setter != null; }

    public boolean isReadable() { return getter != null; }
}
