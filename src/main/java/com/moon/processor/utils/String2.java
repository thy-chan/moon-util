package com.moon.processor.utils;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author benshaoye
 */
public enum String2 {
    ;

    public static String decapitalize(String name) { return Introspector.decapitalize(name); }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String indent(int indent) {
        char[] chars = new char[indent];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public static String keyOf(String... keys) {
        return String.join(":", keys);
    }

    public static String format(String template, Object... values) {
        if (values != null) {
            List<String> rest = null;
            int index = 0;
            for (Object value : values) {
                if (template.contains("{}")) {
                    template = template.replaceFirst("\\{\\}", value == null ? "null" : value.toString());
                } else {
                    if (rest == null) {
                        rest = new ArrayList<>();
                    }
                    rest.add(index + ": " + value);
                }
                index++;
            }
            if (rest != null) {
                template += rest.toString();
            }
        }
        return template;
    }

    public static StringBuilder newLine(StringBuilder builder) {
        return builder.append('\n');
    }

    public static StringBuilder newLine(StringBuilder builder, String indent) {
        return newLine(builder).append(indent);
    }

    public static boolean isBlank(String str) {
        if (isEmpty(str)) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) { return !isBlank(str); }

    public static boolean isEmpty(String str) { return str == null || str.isEmpty(); }

    public static boolean isNotEmpty(String str) { return !isEmpty(str); }

    public static String replaceAll(String input, String search, String replacement) {
        if (isEmpty(input) || isEmpty(search) || replacement == null) {
            return input;
        }
        int index = input.indexOf(search);
        if (index == -1) {
            return input;
        }
        int capacity = input.length();
        if (replacement.length() > search.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);
        int idx = 0;
        int searchLen = search.length();
        while (index >= 0) {
            sb.append(input, idx, index);
            sb.append(replacement);
            idx = index + searchLen;
            index = input.indexOf(search, idx);
        }
        sb.append(input, idx, input.length());
        return sb.toString();
    }

    private final static HolderGroup GROUP = Holder.of(Holder.type, Holder.name, Holder.field);

    public static String toConstField(String type, String name, String value) {
        // 这里实际应该是 {value}，为了重用 GROUP，就写成了 {field}
        String template = "private final static {type} {name} = {field};";
        return GROUP.on(template, type, name, value);
    }

    public static String toPrivateField(String field, String type) {
        String template = "private {type} {field};";
        return GROUP.on(template, type, "", field);
    }

    public static String toGetterName(String field, String type) {
        return ("boolean".equals(type) ? Const2.IS : Const2.GET) + capitalize(field);
    }

    public static String toGetterMethod(String field, String type) {
        return toGetterMethod(toGetterName(field, type), field, type);
    }

    public static String toGetterMethod(String getterName, String field, String type) {
        String template = "public {type} {name}() { return this.{field}; }";
        return GROUP.on(template, type, getterName, field);
    }

    public static String toSetterName(String field) {
        return Const2.SET + capitalize(field);
    }

    public static String toSetterMethod(String field, String type) {
        return toSetterMethod(toSetterName(field), field, type);
    }

    public static String toSetterMethod(String setterName, String field, String type) {
        String template = "public void {name}({type} {field}) { this.{field} = {field}; }";
        return GROUP.on(template, type, setterName, field);
    }

    public static String camelcaseToHyphen(String str, char hyphen, boolean continuousSplit) {
        final int len = str == null ? 0 : str.length();
        if (len == 0) { return str; }
        boolean prevIsUpper = false, thisIsUpper;
        StringBuilder res = new StringBuilder(len + 5);
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (thisIsUpper = Character.isUpperCase(ch)) {
                ch = Character.toLowerCase(ch);
                if (i == 0) {
                    res.append(ch);
                } else if (prevIsUpper) {
                    if (continuousSplit) {
                        res.append(hyphen).append(ch);
                    } else if (Character.isLowerCase(str.charAt(i + 1))) {
                        res.append(hyphen).append(ch);
                    } else {
                        res.append(ch);
                    }
                } else {
                    res.append(hyphen).append(ch);
                }
            } else if (!Character.isWhitespace(ch)) {
                res.append(ch);
            }
            prevIsUpper = thisIsUpper;
        }
        return res.toString();
    }



    public static String toGeneralizableType(String type) {
        switch (type) {
            case "byte":
                return "java.lang.Byte";
            case "short":
                return "java.lang.Short";
            case "int":
                return "java.lang.Integer";
            case "long":
                return "java.lang.Long";
            case "float":
                return "java.lang.Float";
            case "double":
                return "java.lang.Double";
            case "boolean":
                return "java.lang.Boolean";
            case "char":
                return "java.lang.Character";
            default:
                return type;
        }
    }

    public static String onInlineCommentOf(String comment) {
        return format("/* {} */", comment);
    }

    public static String onInlineDocCommentOf(String comment) {
        return format("/** {} */", comment);
    }

    public static String[] onBlockCommentOf(String... comments) {
        String[] results = new String[comments.length + 2];
        int index = 0;
        results[index++] = "/*";
        for (String comment : comments) {
            results[index++] = " * " + comment;
        }
        results[index] = " */";
        return results;
    }
}
