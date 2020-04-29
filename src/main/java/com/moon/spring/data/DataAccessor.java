package com.moon.spring.data;

import com.moon.more.model.id.IdSupplier;

/**
 * @author benshaoye
 */
public interface DataAccessor<ID, T extends IdSupplier<ID>> extends BaseAccessor<ID, T> {

    /**
     * 逻辑删除
     *
     * @param id
     */
    void disableById(ID id);

    /**
     * 逻辑删除
     *
     * @param entity
     */
    void disable(T entity);

    /**
     * 逻辑删除
     *
     * @param entities
     */
    void disableAll(Iterable<? extends T> entities);

    /**
     * 逻辑删除
     *
     * @param first
     * @param second
     * @param entities
     * @param <S>
     */
    <S extends T> void disableAll(S first, S second, S... entities);
}