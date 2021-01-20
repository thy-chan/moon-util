package com.moon.accessor.meta;

/**
 * @author benshaoye
 */
public interface Table<R, TB extends Table<R, TB>> {

    /**
     * 返回实体数据类型
     *
     * @return 实体 java 类型
     */
    Class<R> getEntityClass();

    /**
     * 返回实体数据量格式化类名
     *
     * @return 实体完整类名
     */
    default String getEntityClassname() { return getEntityClass().getCanonicalName(); }

    /**
     * 返回数据库表名
     *
     * @return 表名
     */
    String getTableName();

    /**
     * 返回实体对应数据表的列数
     *
     * @return 数据表列数
     */
    int getTableFieldsCount();

    /**
     * 获取所有字段
     *
     * @return 数据表所有字段
     */
    TableField<?, R, TB>[] getTableFields();
}
