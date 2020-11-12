package com.moon.mapping.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author moonsky
 */
@SuppressWarnings("all")
final class MapMethodFactory {

    private final ProcessingEnvironment env;
    private final AtomicInteger indexer = new AtomicInteger();

    MapMethodFactory() { this.env = EnvUtils.getEnv(); }

    public AtomicInteger getIndexer() { return indexer; }

    final String newThatOnEmptyConstructor(String thisType, String thatType) {
        return replaceTypeAndName(MapScripts.newThatAsEmpty, thisType, thatType);
    }

    final String newThisOnEmptyConstructor(String thisType, String thatType) {
        return replaceTypeAndName(MapScripts.newThisAsEmpty, thisType, thatType);
    }

    final String copyBackwardField(
        Mappable fromProperty, Mappable toProperty
    ) {
        return copyBackwardField(toProperty.getSetterName(),//
            fromProperty.getGetterName(),//
            toProperty.getSetterFinalType(),//
            fromProperty.getGetterFinalType(),//
            toProperty.isPrimitiveSetter());
    }

    private final String copyBackwardField(
        String setterName, String getterName, String setterType, String getterType, boolean primitive
    ) {
        if (DetectUtils.isAnyNull(setterName, getterName, setterType, getterType)) {
            return "";
        }
        String result = "self.{setterName}(({setterType}) that.{getterName}());";
        // if (Objects.equals(getterType, setterType)) {
        //     result = "self.{setterName}(that.{getterName}());";
        // }
        for (Defaults value : Defaults.values()) {
            if (value.isTypeMatches(getterType, setterType)) {
                result = value.getFromThatTemplate(getIndexer());
                if (!primitive) {
                    result += Defaults.copyForwardNull;
                }
                break;
            }
        }
        result = Replacer.getterType.replace(result, getterType);
        result = Replacer.setterType.replace(result, setterType);
        result = Replacer.setterName.replace(result, setterName);
        return Replacer.getterName.replace(result, getterName);
    }

    final String copyForwardMethod(String thisType, String thatType) {
        String result = Replacer.thisType.replace(MapScripts.doBackward, thisType);
        return Replacer.thatType.replace(result, thatType);
    }

    final String copyForwardField(
        Mappable fromProperty, Mappable toProperty
    ) {
        return copyForwardField(toProperty.getSetterName(),//
            fromProperty.getGetterName(),//
            toProperty.getSetterFinalType(),//
            fromProperty.getGetterFinalType(),//
            toProperty.isPrimitiveSetter());
    }

    private final String copyForwardField(
        String setterName, String getterName, String setterType, String getterType, boolean primitive
    ) {
        if (DetectUtils.isAnyNull(setterName, getterName, setterType, getterType)) {
            return "";
        }
        String result = "that.{setterName}(({setterType}) self.{getterName}());";
        // if (Objects.equals(getterType, setterType)) {
        //     result = "that.{setterName}(self.{getterName}());";
        // }
        for (Defaults value : Defaults.values()) {
            if (value.isTypeMatches(getterType, setterType)) {
                result = value.getToThatTemplate(getIndexer());
                if (!primitive) {
                    result += Defaults.copyForwardNull;
                }
                break;
            }
        }
        result = Replacer.getterType.replace(result, getterType);
        result = Replacer.setterType.replace(result, setterType);
        result = Replacer.setterName.replace(result, setterName);
        return Replacer.getterName.replace(result, getterName);
    }

    final String copyBackwardMethod(String thisType, String thatType) {
        String result = Replacer.thisType.replace(MapScripts.doForward, thisType);
        return Replacer.thatType.replace(result, thatType);
    }

    final String fromMapField(Mappable prop) {
        if (DetectUtils.isAnyNull(prop.getSetterName(), prop.getSetterFinalType())) {
            return "";
        }
        String t0 = "self.{setterName}(({setterType}) thatObject.get(\"{name}\"));";
        if (prop.isPrimitiveSetter()) {
            t0 = "{setterType} {var} = ({setterType}) thatObject.get(\"{name}\");" +//
                "if ({var} != null) { self.{setterName}({var}); }";
            t0 = Replacer.var.replace(t0, nextVarname());
            t0 = Replacer.setterType.replace(t0, prop.getWrappedSetterType());
        } else if (Objects.equals(prop.getSetterFinalType(), "java.lang.String")) {
            t0 = "Object {var} = thatObject.get(\"{name}\");" +//
                "if ({var} == null) { self.{setterName}(null); }" +//
                "else { self.{setterName}({var}.toString()); }";
            t0 = Replacer.var.replace(t0, nextVarname());
        }
        t0 = Replacer.setterType.replace(t0, prop.getSetterFinalType());
        t0 = Replacer.setterName.replace(t0, prop.getSetterName());
        return Replacer.name.replace(t0, prop.getName());
    }

    final String fromMapMethod(String thisType, Iterable<String> fields) {
        String result = Replacer.thisType.replace(MapScripts.fromMap, thisType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    final String toMapField(Mappable property) {
        return toMapField(property.getName(), property.getGetterName());
    }

    private final String toMapField(String name, String getterName) {
        String t0 = "thatObject.put(\"{name}\", self.{getterName}());";
        String result = Replacer.name.replace(t0, name);
        return Replacer.getterName.replace(result, getterName);
    }

    final String toMapMethod(String thisType, Iterable<String> fields) {
        String result = Replacer.thisType.replace(MapScripts.toMap, thisType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    final String newThisAsMapMethod(String thisType) {
        return Replacer.thisType.replace(MapScripts.newThisAsMap, thisType);
    }

    private Elements elements() { return env.getElementUtils(); }

    final String toStringField(Mappable model, boolean first) {
        return toStringField(model.getName(), model.getGetterName(), first);
    }

    private final String toStringField(String name, String getterName, boolean first) {
        String t0 = "builder.append(\"{name}=\").append(self.{getterName}());";
        String t1 = "builder.append(\", {name}=\").append(self.{getterName}());";
        String template = Replacer.name.replace(first ? t0 : t1, name);
        return Replacer.getterName.replace(template, getterName);
    }

    final String toStringMethod(String thisType, Iterable<String> fields) {
        String result = Replacer.thisType.replace(MapScripts.toString, thisType);
        result = Replacer.thisName.replace(result, thisType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    final String safeCopyForwardMethod(String thisType, String thatType, Iterable fields) {
        String result = Replacer.thisType.replace(MapScripts.safeCopyForward, thisType);
        result = Replacer.thatType.replace(result, thatType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    final String safeCopyBackwardMethod(String thisType, String thatType, Iterable fields) {
        String result = Replacer.thisType.replace(MapScripts.safeCopyBackward, thisType);
        result = Replacer.thatType.replace(result, thatType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    final String cloneField(Mappable property) {
        if (property.hasGetterMethod() && property.hasSetterMethod()) {
            String result = "self.{setterName}(that.{getterName}());";
            result = Replacer.setterName.replace(result, property.getSetterName());
            return Replacer.getterName.replace(result, property.getGetterName());
        }
        return "";
    }

    final String cloneMethod(String thisType, String implType, Iterable<String> fields) {
        String result = Replacer.thisType.replace(MapScripts.clone, thisType);
        result = Replacer.implType.replace(result, implType);
        return Replacer.MAPPINGS.replace(result, String.join("", fields));
    }

    private static String replaceTypeAndName(String template, String thisType, String thatType) {
        String result = Replacer.thisType.replace(template, thisType);
        result = Replacer.thatType.replace(result, thatType);
        result = Replacer.thisName.replace(result, thisType);
        result = Replacer.thatName.replace(result, thatType);
        return result;
    }

    private final static Replacer[] REPLACERS = Replacer.values();

    private final String nextVarname() {
        return nextVarname(getIndexer());
    }

    private final static String nextVarname(AtomicInteger indexer) {
        return "var" + indexer.getAndIncrement();
    }

    private enum Replacer {
        MAPPINGS,
        var,
        name,
        getterName,
        setterName,
        getterType,
        setterType,
        thisType,
        implType,
        thatType,
        thisName {
            @Override
            String toReplacement(String value) { return ElementUtils.getSimpleName(value); }
        },
        thatName {
            @Override
            String toReplacement(String value) { return ElementUtils.getSimpleName(value); }
        },
        ;

        private final String pattern;

        Replacer() { this.pattern = "\\{" + name() + "\\}"; }

        String toReplacement(String value) { return value; }

        public String replace(String template, String type) {
            return template.replaceAll(pattern, String.valueOf(toReplacement(type)));
        }
    }

    private enum Defaults {
        character("Object {var}=self.{getterName}();" +//
            "if ({var} != null) { that.{setterName}((java.lang.Character){var});}",//
            "char", "Character", "java.lang.Character") {},
        string("Object {var}=self.{getterName}();" +//
            "if ({var} != null) { that.{setterName}({var}.toString()); }",//
            "String", "java.lang.String") {
            @Override
            boolean isGetterTypeMatches(String getterType) { return true; }
        },
        byteByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Byte.parseByte({var}.toString(), 10));" +//
            "}", "byte", "Byte", "java.lang.Byte"),
        shortByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Short.parseShort({var}.toString(), 10));" +//
            "}", "short", "Short", "java.lang.Short"),
        intByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Integer.parseInt({var}.toString(), 10));" +//
            "}", "int", "Integer", "java.lang.Integer"),
        longByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Long.parseLong({var}.toString(), 10));" +//
            "}", "long", "Long", "java.lang.Long"),
        floatByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Float.parseFloat({var}.toString(), 10));" +//
            "}", "float", "Float", "java.lang.Float"),
        doubleByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Double.parseDouble({var}.toString(), 10));" +//
            "}", "double", "Double", "java.lang.Double"),
        booleanByString("CharSequence {var}=self.{getterName}();" +//
            "if ({var} != null) { " +//
            "    that.{setterName}(Boolean.parseBoolean({var}.toString(), 10));" +//
            "}", "boolean", "Boolean", "java.lang.Boolean");

        private final static String copyBackwardNull = " else { self.{setterName}(null); }";
        private final static String copyForwardNull = " else { that.{setterName}(null); }";
        private final String[] setterTypes;
        private final String fromThat;
        private final String toThat;

        Defaults(String template, String... setterTypes) {
            this.setterTypes = setterTypes;
            this.toThat = template;
            Object value = template;
            this.fromThat = template//
                .replaceAll("self", "PLACEHOLDER")//
                .replaceAll("that", "self")//
                .replaceAll("PLACEHOLDER", "that");
        }

        String getToThatTemplate(AtomicInteger indexer) {
            return Replacer.var.replace(toThat, nextVarname(indexer));
        }

        String getFromThatTemplate(AtomicInteger indexer) {
            return Replacer.var.replace(fromThat, nextVarname(indexer));
        }

        boolean isTypeMatches(String getterType, String setterType) {
            return isGetterTypeMatches(getterType) && isSetterTypeMatches(setterType);
        }

        boolean isGetterTypeMatches(String getterType) {
            return strings().contains(getterType);
        }

        boolean isSetterTypeMatches(String setterType) {
            for (String type : setterTypes) {
                if (Objects.equals(type, setterType)) {
                    return true;
                }
            }
            return false;
        }

        static Set<String> strings() {
            Set<String> types = new HashSet<>();
            types.add("java.lang.StringBuilder");
            types.add("java.lang.StringBuffer");
            types.add("java.lang.String");
            types.add("StringBuilder");
            types.add("StringBuffer");
            types.add("String");
            return types;
        }
    }
}