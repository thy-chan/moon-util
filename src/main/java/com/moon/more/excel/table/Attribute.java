package com.moon.more.excel.table;

import com.moon.core.lang.ArrayUtil;
import com.moon.more.excel.PropertyControl;
import com.moon.more.excel.annotation.TableColumn;
import com.moon.more.excel.annotation.TableColumnGroup;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author benshaoye
 */
final class Attribute implements Descriptor, Comparable<Attribute> {

    private final Marked onField;
    private final Marked onMethod;

    Attribute(Marked onMethod, Marked onField) {
        this.onMethod = onMethod;
        this.onField = onField;
        Assert.notDuplicated(this);
    }

    private final <T> T obtainOrNull(Marked marked, Function<Marked, T> getter) {
        return marked == null ? null : getter.apply(marked);
    }

    private final <T> T obtainOrNull(Function<Marked, T> getter) {
        T value = obtainOrNull(onMethod, getter);
        return value == null ? obtainOrNull(onField, getter) : value;
    }

    public PropertyControl getValueGetter() {
        Method method = this.onMethod == null ? null : (Method) this.onMethod.getMember();
        Field field = this.onField == null ? null : (Field) this.onField.getMember();
        return ValueGetter.of(method, field);
    }

    public TransformForGet getTransformOrDefault() {
        return TransformForGet.findOrDefault(getPropertyType());
    }

    @Override
    public String[] getTitles() {
        String[] titles = obtainOrNull(m -> m.getTitles());
        return titles == null ? ArrayUtil.toArray(getName()) : titles;
    }

    @Override
    public short[] getHeadHeightArr() {
        return obtainOrNull(m -> m.getHeadHeightArr());
    }

    @Override
    public Integer getColumnWidth() {
        TableColumn column = getTableColumn();
        return column == null ? null : column.width();
    }

    @Override
    public String getName() {
        return obtainOrNull(m -> m.getName());
    }

    @Override
    public Class getPropertyType() {
        return obtainOrNull(m -> m.getPropertyType());
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return (T) obtainOrNull(m -> m.getAnnotation(annotationType));
    }

    @Override
    public TableColumn getTableColumn() {
        return obtainOrNull(m -> m.getTableColumn());
    }

    @Override
    public TableColumnGroup getTableColumnGroup() {
        return obtainOrNull(m -> m.getTableColumnGroup());
    }

    public int getOrder() {
        return getOrDefault(TableColumn::order, TableColumnGroup::order, 0);
    }

    @Override
    public int compareTo(Attribute o) { return this.getOrder() - o.getOrder(); }
}
