package com.moon.spring.jpa;

import static com.moon.core.lang.StringUtil.repeat;

/**
 * @author benshaoye
 */
public interface ISystemOut {

    default void outputNextLineOf150(char ch) {
        outputNextLineOf150(String.valueOf(ch));
    }

    default void outputNextLineOf150(String ch) {
        outputNextLine(ch, 150);
    }

    default void outputNextLine(String ch, int count) {
        System.out.println(repeat(ch, count));
    }
}
