package com.moon.accessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author benshaoye
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TableColumnPolicy {

    String pattern() default "{}";

    CasePolicy casePolicy() default CasePolicy.UNDERSCORE_LOWERCASE;
}