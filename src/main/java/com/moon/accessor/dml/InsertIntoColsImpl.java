package com.moon.accessor.dml;

import com.moon.accessor.meta.Table;
import com.moon.accessor.meta.TableField;

import java.util.Collection;

/**
 * @author benshaoye
 */
public class InsertIntoColsImpl<T1, T2, R, TB extends Table<R, TB>>//
    extends InsertIntoBase<R, TB>//
    implements InsertInto<R, TB>, InsertIntoCol1<T1, R, TB>, InsertIntoCol2<T1, T2, R, TB> {

    @SafeVarargs
    public InsertIntoColsImpl(TB table, TableField<?, R, TB>... fields) {
        super(table, fields);
    }

    @Override
    public InsertIntoValuesImpl<T1, T2, R, TB> values(T1 v1) { return insertValuesOf(v1); }

    @Override
    public InsertIntoValuesImpl<T1, T2, R, TB> values(T1 v1, T2 v2) { return insertValuesOf(v1, v2); }

    private InsertIntoValuesImpl<T1, T2, R, TB> insertValuesOf(Object... values) {
        return new InsertIntoValuesImpl<>(getTable(), getFields(), values);
    }

    @Override
    public InsertIntoValuesImpl<T1, T2, R, TB> valuesRecord(R record) {
        return new InsertIntoValuesImpl<>(getTable(), getFields(), asList(record));
    }

    @Override
    public InsertIntoValuesImpl<T1, T2, R, TB> valuesRecord(R record1, R record2, R... records) {
        return new InsertIntoValuesImpl<>(getTable(), getFields(), asList(record1, record2, records));
    }

    @Override
    public InsertIntoValuesImpl<T1, T2, R, TB> valuesRecord(Collection<? extends R> records) {
        return new InsertIntoValuesImpl<>(getTable(), getFields(), records);
    }

    @Override
    public SelectCol1<T1> select(
        TableField<T1, ?, ? extends Table<?, ?>> f1
    ) { return null; }

    @Override
    public SelectCol2<T1, T2> select(
        TableField<T1, ?, ? extends Table<?, ?>> f1, TableField<T2, ?, ? extends Table<?, ?>> f2
    ) { return null; }
}
