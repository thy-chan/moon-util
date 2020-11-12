package com.moon.mapping.processing;

import com.moon.mapping.BeanMapping;
import com.moon.mapping.annotation.MappingFor;

import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.moon.mapping.processing.DetectUtils.isUsable;

/**
 * @author benshaoye
 */
abstract class BaseDefinition<M extends BaseMethod, P extends BaseProperty<M>> extends LinkedHashMap<String, P>
    implements Completable, CanonicalNameable {

    protected final static String IMPL_SUFFIX = "ImplGeneratedByMoonUtil";

    private final MapMethodFactory factory = new MapMethodFactory();

    /**
     * 声明注解{@link com.moon.mapping.annotation.MappingFor}的类
     */
    private final TypeElement thisElement;

    public BaseDefinition(TypeElement enclosingElement) { this.thisElement = enclosingElement; }

    public final TypeElement getThisElement() { return thisElement; }

    public final boolean isInterface() { return getThisElement().getKind().isInterface(); }

    public final MapMethodFactory getFactory() { return factory; }

    @Override
    public final String getCanonicalName() { return ElementUtils.getQualifiedName(getThisElement()); }

    /**
     * 声明{@link MappingFor}的类{@link #getThisElement()}所在包的完整名
     *
     * @return 包名
     */
    public final String getPackageName() { return ElementUtils.getPackageName(getThisElement()); }

    /**
     * {@link #getThisElement()}的类名，如果是内部类，也只返回最后一部分
     *
     * @return
     */
    @Override
    public final String getSimpleName() { return ElementUtils.getSimpleName(getThisElement()); }

    /**
     * {@link #getThisElement()}的完整合法名称
     *
     * @return
     */
    public final String getQualifiedName() { return ElementUtils.getQualifiedName(getThisElement()); }

    @Override
    public void onCompleted() { forEach((name, prop) -> prop.onCompleted()); }

    /**
     * 实现{@link BeanMapping}的公共方法
     */
    public StringAdder implMappingSharedMethods() {
        final StringAdder adder = new StringAdder();
        addObjectMapping(adder);
        addMapMapping(adder);
        return adder;
    }

    final void addBeanMapping(StringAdder adder, BaseDefinition thatDef) {
        adder.add("TO_" + StringUtils.underscore(thatDef.getCanonicalName())).add(" {");
        build$safeWithThat(adder, thatDef);
        final String thisClassname = getSimpleName();
        final String thatClassname = thatDef.getSimpleName();
        adder.add(getFactory().copyForwardMethod(thisClassname, thatClassname));
        adder.add(getFactory().copyBackwardMethod(thisClassname, thatClassname));
        adder.add(getFactory().newThatOnEmptyConstructor(thisClassname, thatClassname));
        adder.add(getFactory().newThisOnEmptyConstructor(thisClassname, thatClassname));
        adder.add("},");
    }

    private void build$safeWithThat(StringAdder adder, BaseDefinition thatDef) {
        final String thatClass = thatDef.getSimpleName();
        final String thisClass = getSimpleName();
        final MapMethodFactory factory = getFactory();
        {
            Collection<String> fields = reducing(thisProp -> {
                Mappable thatProp = (Mappable) thatDef.get(thisProp.getName());
                if (isUsable(thisProp, thatProp)) {
                    return factory.copyForwardField(thisProp, thatProp);
                }
                return null;
            });
            adder.add(factory.safeCopyForwardMethod(thisClass, thatClass, fields));
        }
        {
            Collection<String> fields = reducing(thisProp -> {
                Mappable thatProp = (Mappable) thatDef.get(thisProp.getName());
                if (isUsable(thatProp, thisProp)) {
                    return factory.copyBackwardField(thatProp, thisProp);
                }
                return null;
            });
            adder.add(factory.safeCopyBackwardMethod(thisClass, thatClass, fields));
        }
    }

    private void addMapMapping(final StringAdder adder) {
        {
            // fromMap(Object,Map)
            Collection<String> fields = reducing(getFactory()::fromMapField);
            adder.add(getFactory().fromMapMethod(getSimpleName(), fields));
        }
        {
            // toMap(Object,Map)
            Collection<String> fields = reducing(getFactory()::toMapField);
            adder.add(getFactory().toMapMethod(getSimpleName(), fields));
        }
        {
            // newThis(Map)
            adder.add(getFactory().newThisAsMapMethod(getSimpleName()));
        }
    }

    private void addObjectMapping(final StringAdder adder) {
        {
            // clone(Object)
            Collection<String> fields = reducing(getFactory()::cloneField);
            adder.add(getFactory().cloneMethod(getSimpleName(), getSimpleName(), fields));
        }
        {
            // toString(Object)
            @SuppressWarnings("all") Collection<String> fields = reducing((list, property) ->//
                getFactory().toStringField(property, list.isEmpty()));
            adder.add(getFactory().toStringMethod(getSimpleName(), fields));
        }
    }

    private Collection<String> reducing(Function<Mappable, String> serializer) {
        return reducing((m, prop) -> serializer.apply(prop));
    }

    private Collection<String> reducing(BiFunction<Map<String, String>, Mappable, String> serializer) {
        final Map<String, String> parsedFields = new LinkedHashMap<>();
        final Set<String> sortKeys = getSortedKeys();
        for (String key : sortKeys) {
            Mappable property = get(key);
            if (property != null) {
                String field = serializer.apply(parsedFields, property);
                if (isNotBlank(field)) {
                    parsedFields.put(key, field);
                }
            }
        }
        return CollectUtils.reduce(entrySet(), (parsed, entry) -> {
            if (sortKeys.contains(entry.getKey())) {
                return parsed;
            }
            String field = serializer.apply(parsed, entry.getValue());
            if (isNotBlank(field)) {
                parsed.put(entry.getKey(), field);
            }
            return parsed;
        }, parsedFields).values();
    }

    private static boolean isNotBlank(String str) {
        if (str == null) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getSortedKeys() {
        Set<String> sorts = new LinkedHashSet<>();
        sorts.add("id");
        return sorts;
    }
}