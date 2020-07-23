package com.moon.more.validator.impl;

import com.moon.core.enums.Testers;
import com.moon.more.validator.annotation.RequireTesterOf;

/**
 * @author moonsky
 */
public class RequireTesterValidator extends RequireValidator<RequireTesterOf> {

    public RequireTesterValidator() { }

    @Override
    public void initialize(RequireTesterOf constraintAnnotation) {
        Testers tester = constraintAnnotation.value();
        boolean not = constraintAnnotation.not();
        setTester(not ? tester.not : tester);
    }
}
