package com.moon.processor.utils;

import com.moon.processor.model.DeclareMethod;

import javax.lang.model.element.ExecutableElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author benshaoye
 */
public enum Assert2 {
    ;

    public static void assertInterfaceMethod(ExecutableElement element, boolean isAbstract) {

    }

    public static boolean assertEffectMethod(DeclareMethod getter, DeclareMethod setter) {
        if (getter.isDefaultMethod()) {
            if (setter != null && !setter.isDefaultMethod()) {
                String msg = "无法定义 setter 方法: {}({})";
                msg = String2.format(msg, setter.getName(), setter.getDeclareType());
                throw new IllegalStateException(msg);
            }
            return false;
        }
        return true;
    }

    public static void assertNonAbstractMethod(DeclareMethod method, String getterOrSetter) {
        if (method.isAbstractMethod()) {
            String msg = "无法定义 {} 方法: {}({})";
            msg = String2.format(msg, getterOrSetter, method.getName(), method.getDeclareType());
            throw new IllegalStateException(msg);
        }
    }

    public static boolean assertAbstractMethod(DeclareMethod method, String getterOrSetter) {
        if (method.isAbstractMethod()) {
            return true;
        }
        String msg = "无法定义 {} 方法: {}({})";
        msg = String2.format(msg, getterOrSetter, method.getName(), method.getDeclareType());
        throw new IllegalStateException(msg);
    }

    @SuppressWarnings("all")
    public static void assertNonSetters(Map<String, DeclareMethod> allSettersMap, String declaredType) {
        Map<String, DeclareMethod> settersMap = new HashMap<>(allSettersMap);
        settersMap.remove(declaredType);
        if (settersMap.isEmpty()) {
            return;
        }
        List<DeclareMethod> setters = settersMap.values().stream().filter(it -> !it.isDefaultMethod())
            .collect(Collectors.toList());
        if (setters.isEmpty()) {
            return;
        }
        throw new IllegalStateException(//
            "无法定义 setter 方法: " + setters//
                .stream()//
                .map(s -> String2.format("{}({})", s.getName(), s.getDeclareType())).collect(Collectors.joining(", ")));
    }
}