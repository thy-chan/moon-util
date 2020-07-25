package com.moon.core.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author moonsky
 */
public interface Traveller<T> extends Supplier<List<T>>, List<T> {

    /**
     * 遍历指定目录下的文件
     *
     * @param path 目标绝对路径
     *
     * @return 当前文件遍历器
     */
    Traveller<T> traverse(String path);

    /**
     * 遍历指定目录下的文件
     *
     * @param path 绝对路径
     *
     * @return 当前文件遍历器
     */
    Traveller<T> traverse(File path);

    /**
     * 初始化或重置
     */
    @Override
    void clear();

    /**
     * 获取所有内容
     *
     * @return 当前遍历器
     */
    @Override
    default List<T> get() { return new ArrayList<>(this); }
}
