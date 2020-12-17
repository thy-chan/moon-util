package com.moon.mapping.processing;

import javax.lang.model.element.TypeElement;

/**
 * @author benshaoye
 */
final class BasicDefinition extends BaseDefinition<BasicMethod, BasicProperty> {

    public BasicDefinition(TypeElement enclosingElement,boolean converter) {
        super(enclosingElement, ElemUtils.getQualifiedName(enclosingElement), converter);
    }
}
