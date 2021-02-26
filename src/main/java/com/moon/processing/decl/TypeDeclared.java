package com.moon.processing.decl;

import com.moon.processing.holder.BaseHolder;
import com.moon.processing.holder.Holders;

import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * 接口/普通类
 *
 * @author benshaoye
 */
public class TypeDeclared extends BaseHolder {

    private final TypeElement typeElement;

    private final String typeClassname;

    /**
     * 所有泛型声明，如声明在 java.util.List&lt;T> 上的 T 在实际应用中如果是：
     * <p>
     * List&lt;String> 那么会这样包含:
     * <p>
     * java.util.List#T  ==  java.lang.String
     * java.util.Collection#T  ==  java.lang.String
     * java.util.Iterable#T  ==  java.lang.String
     * ...(所有父类、继承的接口都有其对应的声明和使用时的实际类)
     */
    private final Map<String, GenericDeclared> genericDeclaredMap;
    /**
     * 所有 properties
     * <p>
     * 只包括所有成员字段(自身声明+继承的+接口包含的)及其对应的 getter/setter 方法
     */
    private final Map<String, PropertyDeclared> properties = new LinkedHashMap<>();
    /**
     * 所有静态字段(自身声明+继承的+接口包含的)
     */
    private final Map<String, FieldDeclared> staticFieldsMap = new LinkedHashMap<>();
    /**
     * 所有构造器
     */
    private final Map<String, ConstructorDeclared> constructorsMap = new LinkedHashMap<>();
    /**
     * 除了 properties 中所有方法外的其他所有方法(自身声明+继承的+接口包含的)
     */
    private final Map<String, MethodDeclared> methodsMap = new LinkedHashMap<>();

    private TypeDeclared(
        Holders holders, TypeElement typeElement, Map<String, GenericDeclared> genericDeclaredMap
    ) {
        super(holders);
        this.typeElement = typeElement;
        this.typeClassname = typeElement.getQualifiedName().toString();
        this.genericDeclaredMap = Collections.unmodifiableMap(genericDeclaredMap);
    }

    public static TypeDeclared from(Holders holders, TypeElement typeElement) {
        Map<String, GenericDeclared> thisGenericMap = Generic2.from(typeElement);
        TypeParser parser = new TypeParser(new TypeDeclared(holders, typeElement, thisGenericMap));
        return parser.doParseTypeDeclared();
    }

    public String getTypeClassname() { return typeClassname; }

    public TypeElement getTypeElement() { return typeElement; }

    public Map<String, GenericDeclared> getGenericDeclaredMap() { return genericDeclaredMap; }

    public Map<String, PropertyDeclared> getCopiedProperties() { return new LinkedHashMap<>(properties); }

    public Map<String, FieldDeclared> getStaticFieldsMap() {
        return new LinkedHashMap<>(staticFieldsMap);
    }

    public Map<String, ConstructorDeclared> getConstructorsMap() { return constructorsMap; }

    public Map<String, MethodDeclared> getMethodsMap() { return methodsMap; }

    public Collection<MethodDeclared> getAllMethodsDeclared() {
        List<MethodDeclared> methods = new ArrayList<>(getMethodsMap().values());
        for (PropertyDeclared propDeclared : properties.values()) {
            methods.addAll(propDeclared.getTypedSetterMap().values());
            methods.add(propDeclared.getGetterMethod());
        }
        return methods;
    }

    void setProperties(Map<String, PropertyDeclared> properties) {
        withAll(this.properties, properties);
    }

    void setStaticFieldsMap(Map<String, FieldDeclared> staticFieldsMap) {
        withAll(this.staticFieldsMap, staticFieldsMap);
    }

    void setMethodsMap(Map<String, MethodDeclared> methodsMap) {
        withAll(this.methodsMap, methodsMap);
    }

    void setConstructorsMap(Map<String, ConstructorDeclared> constructorsMap) {
        withAll(this.constructorsMap, constructorsMap);
    }

    private <K, V> void withAll(Map<K, V> container, Map<K, V> values) {
        container.clear();
        container.putAll(values);
    }

    public Set<String> getAllPropertiesName() { return new LinkedHashSet<>(properties.keySet()); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        TypeDeclared that = (TypeDeclared) o;
        return Objects.equals(getTypeClassname(), that.getTypeClassname());
    }

    @Override
    public int hashCode() { return getTypeClassname().hashCode(); }
}