package com.moon.core.lang;

import org.junit.jupiter.api.Test;

/**
 * @author benshaoye
 */
class CharUtilTestTest {

    @Test
    void testIndexOf() {
        String str1 = "benshaoye";
        String str2 = "shao";

        int index = CharUtil.indexOf(str1.toCharArray(), str2.toCharArray(), 0);
        System.out.println(index);
    }
}