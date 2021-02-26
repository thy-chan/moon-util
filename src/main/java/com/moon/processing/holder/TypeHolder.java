package com.moon.processing.holder;

import com.moon.processing.decl.TypeDeclared;
import com.moon.processing.util.Element2;

import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author benshaoye
 */
public class TypeHolder extends BaseHolder {

    private final Map<String, TypeDeclared> typeDeclaredMap = new HashMap<>();

    public TypeHolder(Holders holders) { super(holders); }

    public TypeDeclared with(TypeElement element) {
        String classname = Element2.getQualifiedName(element);
        TypeDeclared declared = typeDeclaredMap.get(classname);
        if (declared != null) {
            return declared;
        }
        declared = TypeDeclared.from(getHolders(), element);
        typeDeclaredMap.put(classname, declared);
        return declared;
    }
}