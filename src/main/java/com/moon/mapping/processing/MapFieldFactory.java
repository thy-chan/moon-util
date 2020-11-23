package com.moon.mapping.processing;

import com.moon.mapping.annotation.MapProperty;
import org.joda.time.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.moon.mapping.processing.DetectUtils.isTypeof;
import static com.moon.mapping.processing.DetectUtils.isTypeofAny;
import static com.moon.mapping.processing.ElementUtils.getSimpleName;
import static com.moon.mapping.processing.StringUtils.*;

/**
 * @author benshaoye
 */
final class MapFieldFactory {

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final static String DOUBLE = "double";
    private final static String LONG = "long";
    private final static String INT = "int";

    private final static Map<String, String> dateFormats = new HashMap<>();

    private boolean warned = false;

    public MapFieldFactory() {
        dateFormats.put(java.time.LocalDate.class.getName(), "yyyy-MM-dd");
        dateFormats.put(java.time.LocalTime.class.getName(), "HH:mm:ss");
        dateFormats.put(java.time.YearMonth.class.getName(), "yyyy-MM");
        dateFormats.put(java.time.Year.class.getName(), "yyyy");
        dateFormats.put(java.time.MonthDay.class.getName(), "MM-dd");
        if (Imported.JODA_TIME) {
            dateFormats.put(MonthDay.class.getName(), "MM-dd");
            dateFormats.put(YearMonth.class.getName(), "yyyy-MM");
            dateFormats.put(LocalTime.class.getName(), "HH:mm:ss");
            dateFormats.put(LocalDate.class.getName(), "yyyy-MM-dd");
        }
    }

    public String doConvertField(
        final Manager manager
    ) {
        MappingModel model = manager.getModel();
        unWarned();
        String field;
        if (!model.isGetterGeneric() && !model.isSetterGeneric()) {
            field = doConvertDeclaredField(manager);
            if (field == null && isSubtypeOf(model.getGetterDeclareType(), model.getSetterDeclareType())) {
                field = "{toName}.{setterName}({fromName}.{getterName}());";
            }
            if (field == null && !isWarned()) {
                field = warningAndIgnored(model);
            }
        } else if (model.isGetterGeneric() && model.isSetterGeneric()) {
            final String setterTypeString = model.getSetterActualType();
            final String getterTypeString = model.getGetterActualType();
            field = doConvertGenerify(manager, getterTypeString, setterTypeString);
        } else if (model.isGetterGeneric()) {
            final String setterTypeString = model.getSetterDeclareType();
            final String getterTypeString = model.getGetterActualType();
            field = doConvertGenerify(manager, getterTypeString, setterTypeString);
        } else if (model.isSetterGeneric()) {
            final String setterTypeString = model.getSetterActualType();
            final String getterTypeString = model.getGetterDeclareType();
            field = doConvertGenerify(manager, getterTypeString, setterTypeString);
        } else {
            throw new IllegalStateException("This is impossible.");
        }
        return onDeclareCompleted(field, model);
    }

    private String doConvertGenerify(
        Manager manager, String getterTypeString, String setterTypeString
    ) {
        MappingModel model = manager.getModel();
        Elements utils = EnvUtils.getUtils();
        TypeMirror setterType = utils.getTypeElement(setterTypeString).asType();
        TypeMirror getterType = utils.getTypeElement(getterTypeString).asType();
        if (EnvUtils.getTypes().isSubtype(getterType, setterType)) {
            String field = "{toName}.{setterName}(({setterType}){fromName}.{getterName}());";
            return Replacer.setterType.replace(field, manager.onImported(setterTypeString));
        } else {
            return warningAndIgnored(model);
        }
    }

    @SuppressWarnings("all")
    private String doConvertDeclaredField(
        final Manager manager
    ) {
        MappingModel model = manager.getModel();
        String t0 = declareOnDefault(model, manager);
        if (t0 == null) {
            t0 = declare2String(model, manager);
        }
        if (t0 == null) {
            t0 = declare2PrimitiveNumber(model, manager);
        }
        if (t0 == null) {
            t0 = declare2WrappedNumber(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Enum(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Boolean(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Char(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Date(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Calendar(model, manager);
        }
        if (t0 == null) {
            t0 = declare2Jdk8Time(model, manager);
        }
        if (t0 == null) {
            t0 = declare2JodaTime(model, manager);
        }
        if (t0 == null) {
            t0 = declare2BigInteger(model, manager);
        }
        if (t0 == null) {
            t0 = declare2BigDecimal(model, manager);
        }
        return onDeclareCompleted(t0, model);
    }

    private String declareOnDefault(MappingModel model, Manager manager) {
        /** 只有在没有注解{@link MapProperty}时，即没有默认值、没有格式化、没有忽略、没有重命名 */
        if (model.nonAnnotatedMapProperty()) {
            final String setterDeclareType = model.getSetterDeclareType();
            final String getterDeclareType = model.getGetterDeclareType();
            if (Objects.equals(setterDeclareType, getterDeclareType)//
                || isSubtypeOf(getterDeclareType, setterDeclareType)) {
                return manager.ofConvert().onSameType();
            }
        }
        return null;
    }

    /**
     * 已支持：枚举默认值、枚举到字符串数字等默认类型转换、指定属性名
     * <p>
     * 不支持格式化
     *
     * @param model
     * @param manager
     *
     * @return
     */
    private String declare2Enum(MappingModel model, Manager manager) {
        final String setterDeclareType = model.getSetterDeclareType();
        if (!isEnum(setterDeclareType)) {
            return null;
        }
        String t0;
        final MappingManager mapping = manager.getMapping();
        final String getterDeclareType = model.getGetterDeclareType();
        final String dftValue = manager.staticVarForDefaultEnumOnDeclare();
        if (Objects.equals(getterDeclareType, setterDeclareType)) {
            return mapping.onDeclare(null, dftValue);
        }
        if (isString(getterDeclareType)) {
            return mapping.onDeclare("{setterType}.valueOf({var})", dftValue);
        }
        if (isPrimitiveNumber(getterDeclareType)) {
            String enumValues = manager.staticVarForEnumValuesOnDeclare();
            t0 = Replacer.name.replace("{name}[{cast}{var}]", enumValues);
            t0 = Replacer.cast.replace(t0, castPrimitiveIfGtTop(getterDeclareType, INT));
            return mapping.onDeclare(t0, dftValue);
        }
        if (isSubtypeOf(getterDeclareType, Number.class)) {
            String enumValues = manager.staticVarForEnumValuesOnDeclare();
            t0 = Replacer.name.replace("{name}[{var}.intValue()]", enumValues);
            return mapping.onDeclare(t0, dftValue);
        }
        return warningAndIgnored(model);
    }

    /**
     * 所有数据类型到字符串的转换
     * 1. 基本数字类型：String.valueOf(..) + 格式化
     * 2. 数字包装类 + 格式化
     * 3. 枚举
     * 4. jdk8 日期类型 + 格式化
     * 5. Date、Calendar、sql.Date 日期类型 + 格式化
     * 6. Joda 日期类型格式化
     * 7. 其他对象到直接调用 Object.toString()
     *
     * @param model
     * @param manager
     *
     * @return
     */
    private String declare2String(final MappingModel model, Manager manager) {
        final String setterDeclareType = model.getSetterDeclareType();
        if (!isString(setterDeclareType)) {
            return null;
        }
        String t0 = null;
        final MappingManager mapping = manager.getMapping();
        final PropertyAttr attr = model.getAttr();
        final ConvertManager convert = manager.ofConvert();
        final String getterDeclareType = model.getGetterDeclareType();
        final String formatVal = attr.formatValue();
        final String dftValue = manager.staticVarForDefaultString();
        if (isString(getterDeclareType)) {
            return mapping.onDeclare(null, dftValue);
        } else if (StringUtils.isPrimitiveNumber(getterDeclareType)) {
            // getter 是基本数据类型，没有默认值，因为 get 不到 null
            if (formatVal == null) {
                t0 = mapping.onDeclare("{setterType}.valueOf({var})", null);
            } else {
                String stringType = String.class.getCanonicalName();
                String targetType = isCompatible(LONG, getterDeclareType) ? LONG : DOUBLE;
                CallerInfo info = convert.find(stringType, targetType, stringType);
                String mapper = info.toString("{fromName}.{getterName}()", strWrapped(formatVal));
                t0 = mapping.onDeclare(mapper, null);
            }
        } else if (StringUtils.isPrimitive(getterDeclareType)) {
            t0 = mapping.onDeclare("{setterType}.valueOf({var})", null);
        } else if (isEnum(getterDeclareType)) {
            t0 = mapping.onDeclare("{var}.name()", dftValue);
        } else if (isFormattable(formatVal, getterDeclareType, Number.class)) {
            CallerInfo mapper = convert.find(String.class, Number.class, String.class);
            t0 = mapping.onDeclare(mapper.toString(null, strWrapped(formatVal)), dftValue);
        }
        if (t0 == null) {
            final String dateFormat = defaultDatePattern(getterDeclareType, formatVal);
            if (isFormattable(dateFormat, getterDeclareType, Date.class)) {
                CallerInfo mapper = convert.find(String.class, Calendar.class, String.class);
                t0 = mapping.onDeclare(mapper.toString(null, strWrapped(formatVal)), dftValue);
            } else if (isFormattable(dateFormat, getterDeclareType, Calendar.class)) {
                CallerInfo mapper = convert.find(String.class, Calendar.class, String.class);
                t0 = mapping.onDeclare(mapper.toString(null, strWrapped(formatVal)), dftValue);
            } else if (isFormattable(dateFormat, getterDeclareType, TemporalAccessor.class)) {
                String format = manager.staticVarForDateTimeFormatter(dateFormat);
                String fmt = Replacer.format.replace("{format}.format({var})", format);
                t0 = mapping.onDeclare(fmt, dftValue);
            } else if (isJodaFormattable(dateFormat, getterDeclareType)) {
                String format = manager.staticVarForJodaDateTimeFormatter(dateFormat);
                String fmt = Replacer.format.replace("{format}.print({var})", format);
                t0 = mapping.onDeclare(fmt, dftValue);
            }
        }
        if (t0 == null) {
            t0 = mapping.onDeclare("{var}.toString()", dftValue);
        }
        return t0;
    }

    private static String useConvertAndDftValAndFormat(
        MappingModel model, Manager manager, String dftValue, Class<?> getterSuper
    ) {
        return useConvertAndDftValAndFormat(//
            model.getAttr().formatValue(),//
            model.getSetterDeclareType(),//
            manager, dftValue, getterSuper);
    }

    private static String useConvertAndDftValAndFormat(
        String format, String setterType, Manager manager, String dftValue, Class<?> getterSuper
    ) {
        return manager.ofConvert().useConvert(dftValue, info -> {
            return info.toString(null, strWrapped(format));
        }, setterType, getterSuper, String.class);
    }

    /**
     * 支持的数据转换：
     * 1. 基本数据类型之间的数据转换
     * 2. 包装类到基本数据之间的数据转换 + 默认值
     * 3，字符串到基本数据之间的转换 + 格式化 + 默认值
     * 4. Number 到基本数据之间的转换 + 默认值
     * 5. char 到数字之间的转换（不包括{@link Character}）
     * 6. 枚举到数字之间的转换，{@link Enum#ordinal()} + 默认值
     * 7. util Date 到 long/double 的转换 + 默认值
     * 8. jdk8 日期到 long/double 的转换 + 默认值
     * 9. joda 日期到 long/double 的转换 + 默认值
     *
     * @param model
     * @param manager
     *
     * @return
     */
    private String declare2PrimitiveNumber(final MappingModel model, Manager manager) {
        String t0;
        final MappingManager mapping = manager.getMapping();
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (!isPrimitiveNumber(setterDeclareType)) {
            return null;
        }
        if (isPrimitiveNumber(getterDeclareType)) {
            String cast = castPrimitiveIfGtTop(getterDeclareType, setterDeclareType);
            String template = Replacer.cast.replace("{cast}{var}", cast);
            return mapping.onDeclare(template, null);
        }
        if (StringUtils.isPrimitiveChar(getterDeclareType)) {
            String cast = castPrimitiveIfLtLow(setterDeclareType, INT);
            String mapper = Replacer.cast.replace("{cast}{var}", cast);
            return mapping.onDeclare(mapper, null);
        }

        final String dftValue = manager.staticVarForDefaultNumberOnDeclare();
        if (isSubtypeOf(getterDeclareType, Number.class)) {
            return mapping.onDeclare("{var}.{setterType}Value()", dftValue);
        }
        if (isString(getterDeclareType)) {
            String format = model.getAttr().formatValue();
            String setterWrapped = StringUtils.toWrappedType(manager.onImported(setterDeclareType));
            if (isNullString(format)) {
                String name = INT.equals(setterDeclareType) ? "Int" : getSimpleName(setterWrapped);
                String mapper = Replacer.name.replace("{setterType}.parse{name}({var})", name);
                mapper = Replacer.setterType.replace(mapper, setterWrapped);
                return mapping.onDeclare(mapper, dftValue);
            } else {
                CallerInfo info = convert.find(Number.class, String.class, String.class);
                String suffix = Replacer.type0.replace(".{type0}Value()", setterDeclareType);
                String mapper = info.toString(null, strWrapped(format)) + suffix;
                return mapping.onDeclare(mapper, dftValue);
            }
        }
        if (isEnum(getterDeclareType)) {
            String cast = castPrimitiveIfLtLow(setterDeclareType, INT);
            t0 = Replacer.cast.replace("{cast}{var}.ordinal()", cast);
            return mapping.onDeclare(t0, dftValue);
        }

        if (LONG.equals(setterDeclareType)) {
            if (isSubtypeOf(getterDeclareType, Date.class)) {
                return mapping.onDeclare("{var}.getTime()", dftValue);
            } else if (isSubtypeOf(getterDeclareType, Calendar.class)) {
                return mapping.onDeclare("{var}.getTimeInMillis()", dftValue);
            } else if (isTypeofAny(getterDeclareType,
                java.time.LocalDate.class,
                java.time.LocalDateTime.class,
                ZonedDateTime.class,
                OffsetDateTime.class,
                java.time.Instant.class)) {
                CallerInfo info = convert.find(setterDeclareType, getterDeclareType);
                return mapping.onDeclare(info.toString(), dftValue);
            } else if (Imported.JODA_TIME) {
                if (isSubtypeOf(getterDeclareType, ReadableInstant.class)) {
                    return mapping.onDeclare("{var}.getMillis()", dftValue);
                } else if (isTypeofAny(getterDeclareType, LocalDateTime.class, LocalDate.class)) {
                    convert.convertOrWarnedThenNull(dftValue, LONG, getterDeclareType);
                    CallerInfo info = convert.find(LONG, getterDeclareType);
                    if (info != null) {
                        return mapping.onDeclare(info.toString(), dftValue);
                    }
                }
            }
        }
        return warningAndIgnored(model);
    }

    /**
     * 支持的数据转换：
     * 1. 基本数据类型之间的数据转换
     * 2. 包装类到基本数据之间的数据转换 + 默认值
     * 3，字符串到基本数据之间的转换 + 格式化 + 默认值
     * 4. Number 到基本数据之间的转换 + 默认值
     * 5. char 到数字之间的转换（不包括{@link Character}）
     * 6. 枚举到数字之间的转换，{@link Enum#ordinal()} + 默认值
     * 7. util Date 到 long/double 的转换 + 默认值
     * 8. jdk8 日期到 long/double 的转换 + 默认值
     * 9. joda 日期到 long/double 的转换 + 默认值
     *
     * @param model
     * @param manager
     *
     * @return
     */
    private String declare2WrappedNumber(final MappingModel model, Manager manager) {
        String t0;
        final PropertyAttr attr = model.getAttr();
        final MappingManager mapping = manager.getMapping();
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (isWrappedNumber(setterDeclareType)) {
            final String setterPrimitive = toPrimitiveType(setterDeclareType);
            if (isPrimitiveNumber(getterDeclareType)) {
                String cast = getterDeclareType.equals(setterPrimitive) ? "" : bracketed(setterPrimitive);
                return mapping.onDeclare(Replacer.cast.replace("{cast}{var}", cast), null);
            }
            if (StringUtils.isPrimitiveChar(getterDeclareType)) {
                String mapper = Replacer.cast.replace("{cast}{var}", bracketed(setterPrimitive));
                return mapping.onDeclare(mapper, null);
            }
            final String dftValue = manager.staticVarForDefaultNumberOnDeclare();
            if (isWrappedNumber(getterDeclareType)) {
                if (Objects.equals(setterDeclareType, getterDeclareType)) {
                    t0 = mapping.onDeclare(null, dftValue);
                } else {
                    String tx = Replacer.type0.replace("{var}.{type0}Value()", setterPrimitive);
                    t0 = mapping.onDeclare(tx, dftValue);
                }
            } else if (isString(getterDeclareType)) {
                if (attr.formatValue() == null) {
                    String name = INT.equals(setterPrimitive) ? "Int" : getSimpleName(setterDeclareType);
                    String mapper = Replacer.name.replace("{setterType}.parse{name}({var})", name);
                    t0 = mapping.onDeclare(mapper, dftValue);
                } else {
                    CallerInfo info = convert.find(setterDeclareType, String.class, String.class);
                    String mapper = info.toString(null, strWrapped(attr.formatValue()));
                    t0 = mapping.onDeclare(mapper, dftValue);
                }
            } else if (isEnum(getterDeclareType)) {
                String cast = castPrimitiveIfLtLow(setterPrimitive, INT);
                t0 = Replacer.cast.replace("{setterType}.valueOf({cast}{var}.ordinal())", cast);
                t0 = mapping.onDeclare(t0, dftValue);
            } else if (isTypeof(getterDeclareType, Character.class)) {
                String cast = castPrimitiveIfLtLow(setterPrimitive, INT);
                String charVal = Replacer.cast.replace("{setterType}.valueOf({cast}{var}.charValue())", cast);
                t0 = mapping.onDeclare(charVal, dftValue);
            } else if (isSubtypeOf(getterDeclareType, Number.class)) {
                String mapper = Replacer.type0.replace("{var}.{type0}Value()", setterPrimitive);
                t0 = mapping.onDeclare(mapper, dftValue);
            } else if (Long.class.getCanonicalName().equals(setterDeclareType)) {
                if (isSubtypeOf(getterDeclareType, Date.class)) {
                    t0 = mapping.onDeclare("{var}.getTime()", dftValue);
                } else if (isSubtypeOf(getterDeclareType, Calendar.class)) {
                    t0 = mapping.onDeclare("{var}.getTimeInMillis()", dftValue);
                } else if (Imported.JODA_TIME) {
                    if (isSubtypeOf(getterDeclareType, ReadableInstant.class)) {
                        t0 = mapping.onDeclare("{var}.getMillis()", dftValue);
                    } else if (isTypeofAny(getterDeclareType,
                        LocalDateTime.class,
                        LocalDate.class,
                        MutableDateTime.class)) {
                        t0 = mapping.onDeclare("{var}.toDate().getTime()", dftValue);
                    } else {
                        t0 = warningAndIgnored(model);
                    }
                } else {
                    t0 = warningAndIgnored(model);
                }
            } else {
                t0 = warningAndIgnored(model);
            }
        } else if (isTypeof(setterDeclareType, Number.class)) {
            if (isPrimitiveNumber(getterDeclareType)) {
                return mapping.onDeclare("{var}", null);
            } else if (isPrimitiveChar(getterDeclareType)) {
                return mapping.onDeclare("(int){var}", null);
            }
            final String dftValue = manager.staticVarForDefaultNumber(INT);
            if (isSubtypeOf(getterDeclareType, Number.class)) {
                return mapping.onDeclare("{var}", dftValue);
            } else if (isEnum(getterDeclareType)) {
                return mapping.onDeclare("{var}.ordinal()", dftValue);
            } else if (isString(getterDeclareType) && attr.formatValue() != null) {
                CallerInfo info = convert.find(Number.class, String.class, String.class);
                String mapper = info.toString(null, strWrapped(attr.formatValue()));
                t0 = mapping.onDeclare(mapper, dftValue);
            } else {
                t0 = warningAndIgnored(model);
            }
        } else {
            return null;
        }
        return t0;
    }

    /**
     * String -> boolean
     * byte|short|int|long|float|double -> boolean : number != 0
     * Byte|Short|Integer|Long|Float|Double -> boolean {@link Character#forDigit(int, int)}
     * Boolean -> boolean
     *
     * @param model
     *
     * @return
     */
    private String declare2Boolean(final MappingModel model, Manager manager) {
        final String booleanWrappedType = Boolean.class.getCanonicalName();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (!isPrimitiveBoolean(setterDeclareType) && !booleanWrappedType.equals(setterDeclareType)) {
            return null;
        }
        // 基本数据类型
        final MappingManager mapping = manager.getMapping();
        if (isPrimitiveNumber(getterDeclareType)) {
            return mapping.onDeclare("{var} != 0", null);
        } else if (isPrimitiveBoolean(getterDeclareType)) {
            return mapping.onDeclare("{var}", null);
        }

        final String dftValue = manager.staticVarForDefaultBooleanOnDeclare();
        if (isString(getterDeclareType)) {
            String mapper = "{type0}.parseBoolean({var})";
            mapper = Replacer.type0.replace(mapper, manager.onImported(booleanWrappedType));
            return mapping.onDeclare(mapper, dftValue);
        }

        // Number
        if (isWrappedNumber(getterDeclareType)) {
            String staticVar = manager.staticVarForDefaultNumberValueOf(getterDeclareType, "0");
            String mapper = "{name}.equals({var})";
            mapper = Replacer.name.replace(mapper, staticVar);
            return mapping.onDeclare(mapper, dftValue);
        }

        // Boolean
        if (isTypeof(getterDeclareType, Boolean.class)) {
            return mapping.onDeclare("{var}", dftValue);
        }
        return warningAndIgnored(model);
    }

    /**
     * char|Character -> char
     * byte|short|int|long|float|double -> char {@link Character#forDigit(int, int)}
     * Byte|Short|Integer|Long|Float|Double -> char {@link Character#forDigit(int, int)}
     * <p>
     * char|Character -> Character
     * byte|short|int|long|float|double -> Character {@link Character#forDigit(int, int)}
     * Byte|Short|Integer|Long|Float|Double -> Character {@link Character#forDigit(int, int)}
     *
     * @param model
     *
     * @return
     */
    private String declare2Char(final MappingModel model, Manager manager) {
        String t0;
        final String wrappedType = Character.class.getCanonicalName();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (!StringUtils.isPrimitiveChar(setterDeclareType) && !isTypeof(setterDeclareType, wrappedType)) {
            return null;
        }
        final MappingManager mapping = manager.getMapping();
        final ConvertManager convert = manager.ofConvert();
        final String dftValue = manager.staticVarForDefaultChar();
        if (StringUtils.isPrimitiveChar(getterDeclareType)) {
            t0 = convert.onSameType();
        } else if (isTypeof(getterDeclareType, wrappedType)) {
            t0 = convert.onMapping(dftValue, "{var}", setterDeclareType, getterDeclareType);
        } else if (isPrimitiveNumber(getterDeclareType)) {
            t0 = "{toName}.{setterName}(Character.forDigit({cast}{fromName}.{getterName}(), 10));";
            t0 = Replacer.cast.replace(t0, isCompatible(INT, getterDeclareType) ? "" : "(int)");
        } else if (isSubtypeOf(getterDeclareType, Number.class)) {
            String mapper = "{setterType}.forDigit({var}.intValue(), 10)";
            t0 = convert.onMapping(dftValue, mapper, wrappedType, getterDeclareType);
        } else {
            t0 = warningAndIgnored(model);
        }
        return t0;
    }

    /**
     * byte、short、int、long、float、double -> {@link BigDecimal#valueOf(long)}, {@link BigDecimal#valueOf(double)}
     * Byte、Short、Integer、Long、Float、Double -> {@link BigDecimal#valueOf(long)}
     * <p>
     * String -> {@link BigDecimal#BigDecimal(String)}, {@link BigDecimal#BigDecimal(BigInteger)}
     * <p>
     * char|Character -> {@link BigDecimal#valueOf(long)}
     *
     * @param model
     *
     * @return
     */
    private String declare2BigDecimal(final MappingModel model, Manager manager) {
        String t0;
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (!isTypeof(setterDeclareType, BigDecimal.class)) {
            return null;
        }
        final ConvertManager convert = manager.ofConvert();
        final MappingManager mapping = manager.getMapping();
        final String dftValue = manager.staticVarForDefaultBigDecimal();
        if (isTypeof(getterDeclareType, BigDecimal.class)) {
            return mapping.onDeclare(null, dftValue);
        } else if (isPrimitiveNumber(getterDeclareType) || isWrappedNumber(getterDeclareType)) {
            return mapping.onDeclare("{setterType}.valueOf({var})", dftValue);
        } else if (isTypeof(getterDeclareType, BigInteger.class)) {
            return mapping.onDeclare("new {setterType}({var})", dftValue);
        } else if (isSubtypeOf(getterDeclareType, Number.class)) {
            CallerInfo mapper = convert.find(BigDecimal.class, Number.class);
            t0 = mapping.onDeclare(mapper.toString(), dftValue);
        } else if (isEnum(getterDeclareType)) {
            return mapping.onDeclare("{setterType}.valueOf({var}.ordinal())", dftValue);
        } else if (isString(getterDeclareType)) {
            String format = model.getAttr().formatValue();
            if (format == null) {
                String mapper = "new {setterType}({var})";
                return mapping.onDeclare(mapper, dftValue);
            } else {
                CallerInfo mapper = convert.find(setterDeclareType, String.class, String.class);
                t0 = mapping.onDeclare(mapper.toString(null, strWrapped(format)), dftValue);
            }
        } else {
            t0 = warningAndIgnored(model);
        }
        return t0;
    }

    /**
     * byte、short、int、long、float、double -> {@link BigInteger#valueOf(long)}
     * Byte、Short、Integer、Long、Float、Double -> {@link BigInteger#valueOf(long)}
     * String -> {@link BigInteger#BigInteger(String)}
     * char|Character -> {@link BigInteger#valueOf(long)}
     *
     * @param model
     *
     * @return
     */
    private String declare2BigInteger(final MappingModel model, Manager manager) {
        String t0;
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (!isTypeof(setterDeclareType, BigInteger.class)) {
            return null;
        }
        final ConvertManager convert = manager.ofConvert();
        final MappingManager mapping = manager.getMapping();
        final String formatVal = model.getAttr().formatValue();
        final String dftValue = manager.staticVarForDefaultBigInteger();
        if (isTypeof(getterDeclareType, BigInteger.class)) {
            return convert.onSameType(dftValue, getterDeclareType);
        } else if (isPrimitiveNumber(getterDeclareType)) {
            String cast = castPrimitiveIfGtTop(getterDeclareType, LONG);
            String mapper = "{setterType}.valueOf({cast}{var})";
            mapper = Replacer.cast.replace(mapper, cast);
            return mapping.onDeclare(mapper, dftValue);
        } else if (isSubtypeOf(getterDeclareType, Number.class)) {
            return mapping.onDeclare("{setterType}.valueOf({var}.longValue())", dftValue);
        } else if (isEnum(getterDeclareType)) {
            return mapping.onDeclare("{setterType}.valueOf({var}.ordinal())", dftValue);
        } else if (isString(getterDeclareType)) {
            if (formatVal == null) {
                return mapping.onDeclare("new {setterType}({var})", dftValue);
            } else {
                CallerInfo mapper = convert.find(setterDeclareType, String.class, String.class);
                t0 = mapping.onDeclare(mapper.toString(null, strWrapped(formatVal)), dftValue);
            }
        } else {
            t0 = warningAndIgnored(model);
        }
        return t0;
    }

    /**
     * byte|short|int|long|float|double
     * Byte|Short|Integer|Long|Float|Double
     * <p>
     * {@link LocalDateTime#LocalDateTime(long)}
     * {@link LocalDate#LocalDate(long)}
     * {@link LocalTime#LocalTime(long)}
     * {@link Duration#Duration(long)}
     * {@link YearMonth#YearMonth(long)}
     * {@link MonthDay#MonthDay(long)}
     * {@link DateTime#DateTime(long)}
     * {@link Instant#Instant(long)}
     * {@link MutableDateTime#MutableDateTime(long)}
     * {@link MutablePeriod#MutablePeriod(long)}
     *
     * @param model
     *
     * @return
     */
    private String declare2JodaTime(final MappingModel model, Manager manager) {
        if (!Imported.JODA_TIME) {
            return null;
        }
        String t0;
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (isTypeofAny(setterDeclareType,
            MutableDateTime.class,
            DateTime.class,
            LocalDateTime.class,
            LocalDate.class,
            LocalTime.class,
            MutablePeriod.class,
            Duration.class,
            YearMonth.class,
            Instant.class,
            MonthDay.class)) {
            if (isPrimitiveNumber(getterDeclareType)) {
                String cast = isCompatible(LONG, getterDeclareType) ? "" : "(long)";
                t0 = "{toName}.{setterName}(new {setterType}({cast}{fromName}.{getterName}()));";
                t0 = Replacer.cast.replace(t0, cast);
                t0 = Replacer.setterType.replace(t0, manager.onImported(setterDeclareType));
            } else if (isSubtypeOf(getterDeclareType, Number.class)) {
                String mapper = "new {setterType}({var}.longValue())";
                t0 = convert.onMapping(null, mapper, setterDeclareType, getterDeclareType);
            } else if (isTypeof(setterDeclareType, Duration.class)) {
                if (isString(getterDeclareType)) {
                    t0 = convert.onMapping(null, "{setterType}.parse({var})", setterDeclareType, getterDeclareType);
                } else {
                    t0 = warningAndIgnored(model);
                }
            } else if (isTypeofAny(setterDeclareType,
                MutableDateTime.class,
                DateTime.class,
                LocalDateTime.class,
                LocalDate.class,
                LocalTime.class,
                YearMonth.class,
                MonthDay.class,
                Instant.class)) {
                if (isString(getterDeclareType)) {
                    String format = defaultDatePattern(setterDeclareType, model.getAttr().formatValue());
                    String dftFormat = manager.staticVarForJodaDateTimeFormatter(format);
                    if (isNullString(dftFormat)) {
                        t0 = convert.onMapping(null, "{setterType}.parse({var})", setterDeclareType, String.class);
                    } else {
                        t0 = convert.useMapping(null, () -> {
                            String tx = "{setterType}.parse({var}, {format})";
                            return Replacer.format.replace(tx, dftFormat);
                        }, setterDeclareType, String.class);
                    }
                } else if (isSubtypeOf(getterDeclareType, Date.class) || isTypeof(getterDeclareType, Calendar.class)) {
                    t0 = convert.onMapping(null, "new {setterType}({var})", setterDeclareType, getterDeclareType);
                } else {
                    t0 = warningAndIgnored(model);
                }
            } else {
                t0 = warningAndIgnored(model);
            }
            return t0;
        }
        return null;
    }

    private String declare2Jdk8Time(final MappingModel model, Manager manager) {
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (isString(getterDeclareType) && isSubtypeOf(setterDeclareType, TemporalAccessor.class)) {
            String declareVal = defaultDatePattern(setterDeclareType, model.getAttr().formatValue());
            String format = manager.staticVarForDateTimeFormatter(declareVal);
            if (isNullString(format)) {
                return convert.onMapping(null, "{setterType}.parse({var})", setterDeclareType, getterDeclareType);
            } else {
                return convert.useMapping(null, () -> {
                    String fmt = "{setterType}.from({format}.parse({var}))";
                    return Replacer.format.replace(fmt, format);
                }, setterDeclareType, String.class);
            }
        }
        if (!isTypeofJdk8DateTime(setterDeclareType)) {
            return null;
        }
        String t0;
        if (isPrimitiveNumber(getterDeclareType)) {
            String getterType = isCompatible(LONG, getterDeclareType) ? LONG : DOUBLE;
            t0 = convert.useConvert(null,
                info -> info.toString("{fromName}.{getterName}()"),
                setterDeclareType,
                getterType);
        } else if (isSubtypeOf(getterDeclareType, Number.class)) {
            t0 = convert.onConvertSimple(setterDeclareType, Number.class);
        } else if (isSubtypeOf(getterDeclareType, Date.class)) {
            t0 = convert.onConvertSimple(setterDeclareType, Date.class);
        } else if (isTypeof(getterDeclareType, Calendar.class)) {
            t0 = convert.onConvertSimple(setterDeclareType, Calendar.class);
        } else if (Imported.JODA_TIME) {
            if (isSubtypeOf(getterDeclareType, ReadableInstant.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, ReadableInstant.class);
            } else if (isTypeofAny(getterDeclareType, LocalDateTime.class, LocalDate.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, getterDeclareType);
            } else {
                t0 = null;
            }
        } else {
            t0 = null;
        }
        if (t0 == null) {
            t0 = convert.onConvertSimple(setterDeclareType, getterDeclareType);
        }
        return t0 == null ? warningAndIgnored(model) : t0;
    }

    /** long -> Date, Timestamp, java.sql.Date, Calendar */
    private String declare2Date(final MappingModel model, Manager manager) {
        String t0;
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (isTypeofAny(setterDeclareType, Date.class, java.sql.Date.class, Timestamp.class, Time.class)) {
            if (isPrimitiveNumber(getterDeclareType)) {
                String getterType = isCompatible(LONG, getterDeclareType) ? LONG : DOUBLE;
                t0 = convert.useConvert(null, info -> {
                    return info.toString("{fromName}.{getterName}()");
                }, setterDeclareType, getterType);
            } else if (isSubtypeOf(getterDeclareType, Number.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, Number.class);
            } else if (isTypeof(getterDeclareType, Calendar.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, Calendar.class);
            } else if (isTypeofJdk8Date(getterDeclareType)) {
                t0 = convert.onConvertSimple(setterDeclareType, getterDeclareType);
            } else if (isString(getterDeclareType)) {
                final String format = defaultDatePattern(setterDeclareType, model.getAttr().formatValue());
                if (isNullString(format)) {
                    t0 = convert.onConvertSimple(setterDeclareType, String.class);
                } else {
                    t0 = convert.useConvert(null, info -> {
                        return info.toString(null, strWrapped(format));
                    }, setterDeclareType, String.class, String.class);
                }
            } else {
                t0 = onImportedJodaTime(model, convert);
            }
            return t0;
        } else {
            return null;
        }
    }

    private String declare2Calendar(final MappingModel model, Manager manager) {
        String t0;
        final ConvertManager convert = manager.ofConvert();
        final String setterDeclareType = model.getSetterDeclareType();
        final String getterDeclareType = model.getGetterDeclareType();
        if (isTypeof(setterDeclareType, Calendar.class)) {
            if (isPrimitiveNumber(getterDeclareType)) {
                String getterType = isPrimitiveSubtypeOf(getterDeclareType, LONG) ? LONG : DOUBLE;
                t0 = convert.useConvert(null, info -> {
                    return info.toString("{fromName}.{getterName}()");
                }, setterDeclareType, getterType);
            } else if (isSubtypeOf(getterDeclareType, Number.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, Number.class);
            } else if (isSubtypeOf(getterDeclareType, Date.class)) {
                t0 = convert.onConvertSimple(setterDeclareType, Date.class);
            } else if (isTypeofJdk8Date(getterDeclareType)) {
                t0 = convert.onConvertSimple(setterDeclareType, getterDeclareType);
            } else if (isString(getterDeclareType)) {
                final String format = defaultDatePattern(setterDeclareType, model.getAttr().formatValue());
                if (isNullString(format)) {
                    t0 = convert.onConvertSimple(setterDeclareType, String.class);
                } else {
                    t0 = convert.useConvert(null, info -> {
                        return info.toString(null, strWrapped(format));
                    }, setterDeclareType, String.class, String.class);
                }
            } else {
                t0 = onImportedJodaTime(model, convert);
            }
            return t0;
        }
        return null;
    }

    private String defaultDatePattern(String type, String pattern) {
        return pattern == null ? dateFormats.getOrDefault(type, DATE_FORMAT) : pattern;
    }

    private String onImportedJodaTime(MappingModel model, ConvertManager convert) {
        String t0, setterDeclareType = model.getSetterDeclareType(), getterDeclareType = model.getGetterDeclareType();
        if (Imported.JODA_TIME) {
            if (isSubtypeOf(getterDeclareType, ReadableInstant.class)) {
                t0 = convert.useConvert(null, STRINGIFY, setterDeclareType, ReadableInstant.class);
            } else if (isTypeofAny(getterDeclareType, LocalDateTime.class, LocalDate.class)) {
                t0 = convert.useConvert(null, STRINGIFY, setterDeclareType, getterDeclareType);
            } else {
                t0 = warningAndIgnored(model);
            }
        } else {
            t0 = warningAndIgnored(model);
        }
        return t0;
    }

    private String onDeclareCompleted(String t0, MappingModel model) {
        if (t0 != null) {
            t0 = Replacer.fromName.replace(t0, model.getFromName());
            t0 = Replacer.toName.replace(t0, model.getToName());
            t0 = Replacer.setterName.replace(t0, model.getSetterName());
            t0 = Replacer.getterName.replace(t0, model.getGetterName());
            if (t0.contains("{var}")) {
                t0 = Replacer.var.replace(t0, nextVarName());
            }
            if (t0.contains("{var0}")) {
                t0 = Replacer.var0.replace(t0, nextVarName());
            }
            if (t0.contains("{var1}")) {
                t0 = Replacer.var1.replace(t0, nextVarName());
            }
        }
        return t0;
    }

    private String warningAndIgnored(final MappingModel model) {
        String fromType = getSimpleName(model.getFromClassname());
        String toType = getSimpleName(model.getToClassname());
        Object[] values = {
            toType, model.getSetterName(), model.getSetterFinalType(),//
            fromType, model.getGetterName(), model.getGetterFinalType()
        };
        Logger.printWarn(TEMPLATE, values);
        this.warned = true;
        return null;
    }

    private final static String TEMPLATE = "【已忽略】'{}.{}({})' 不兼容 '{}.{}()' 返回值类型: {}";

    private final AtomicInteger indexer = new AtomicInteger();

    private AtomicInteger getIndexer() { return indexer; }

    private boolean isWarned() { return warned; }

    private void unWarned() { this.warned = false; }

    private String nextVarName() { return nextVarName(getIndexer()); }

    private static String nextVarName(AtomicInteger indexer) { return "var" + indexer.getAndIncrement(); }

    private static String castPrimitiveIfGtTop(String thisPrimitive, String topPrimitive) {
        return isPrimitiveSubtypeOf(thisPrimitive, topPrimitive) ? "" : bracketed(topPrimitive);
    }

    private static String castPrimitiveIfLtLow(String thisPrimitive, String topPrimitive) {
        return isCompatible(thisPrimitive, topPrimitive) ? "" : bracketed(thisPrimitive);
    }

    private static boolean isPrimitiveSubtypeOf(String thisType, String thatType) {
        return isCompatible(thatType, thisType);
    }

    private static boolean isCompatible(String thisType, String thatType) {
        String all = "byte,short,int,long,float,double";
        int thisIdx = all.indexOf(thisType);
        int thatIdx = all.indexOf(thatType);
        return thisIdx >= thatIdx;
    }

    private final Function<CallerInfo, String> STRINGIFY = Object::toString;

    private static boolean isString(String value) { return isTypeof(value, String.class); }

    private static boolean isEnum(String value) {
        if (value == null) {
            return false;
        }
        TypeElement elem = EnvUtils.getUtils().getTypeElement(value);
        return elem != null && elem.getKind() == ElementKind.ENUM;
    }

    private static boolean isTypeofJdk8Date(String type) {
        return isTypeofAny(type,
            java.time.LocalDateTime.class,
            java.time.ZonedDateTime.class,
            java.time.OffsetDateTime.class,
            java.time.Instant.class,
            java.time.LocalDate.class);
    }

    private static boolean isTypeofJdk8DateTime(String type) {
        return isTypeofJdk8Date(type) || isTypeof(type, java.time.LocalTime.class);
    }

    private static boolean isSubtypeOf(String actualType, Class<?> superclass) {
        return isSubtypeOf(actualType, superclass.getCanonicalName());
    }

    private static boolean isSubtypeOfAny(String actualType, Class<?>... superclasses) {
        if (superclasses != null) {
            for (Class<?> superclass : superclasses) {
                if (isSubtypeOf(actualType, superclass)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSubtypeOf(String actualType, String superClass) {
        if (actualType == null || superClass == null) {
            return false;
        }
        Elements utils = EnvUtils.getUtils();
        TypeElement elem1 = utils.getTypeElement(actualType);
        TypeElement elem2 = utils.getTypeElement(superClass);
        return elem1 != null && elem2 != null && EnvUtils.getTypes().isSubtype(elem1.asType(), elem2.asType());
    }

    private static boolean isFormattable(String fmt, String actualType, Class<?>... superclasses) {
        return !isNullString(fmt) && isSubtypeOfAny(actualType, superclasses);
    }

    private static boolean isJodaFormattable(String fmt, String type) {
        return Imported.JODA_TIME//
            && isFormattable(fmt, type, ReadableInstant.class, ReadablePartial.class);
    }

    private static boolean isNullString(String dftVal) {
        return dftVal == null;
    }

    private static String strWrapped(String str) { return '"' + str + '"'; }

    private static String bracketed(String str) { return "(" + str + ")"; }
}
