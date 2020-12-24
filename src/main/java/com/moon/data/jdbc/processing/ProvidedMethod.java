package com.moon.data.jdbc.processing;

import com.moon.data.jdbc.annotation.Provided;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.moon.data.jdbc.processing.StringUtils.newLine;

/**
 * @author benshaoye
 */
final class ProvidedMethod {

    private final ExecutableElement element;
    private final Provided provided;
    private final String fieldName;

    public ProvidedMethod(ExecutableElement element, Provided provided) {
        this.fieldName = getPropertyName(element);
        this.element = element;
        this.provided = provided;
    }

    public String getFieldName() { return fieldName; }

    private String getCapitalizedPropertyName() { return StringUtils.capitalize(getFieldName()); }

    private static String getPropertyName(ExecutableElement element) {
        TypeMirror type = element.getReturnType();
        boolean isBool = TestUtils.isTypeKind(type, TypeKind.BOOLEAN);
        String name = StringUtils.getSimpleName(element);
        if (isBool && name.startsWith(Const.IS) && name.length() > 2) {
            name = name.substring(2);
        } else if (name.startsWith(Const.GET) && name.length() > 3) {
            name = name.substring(3);
        } else if (name.startsWith(Const.SET) && name.length() > 3) {
            name = name.substring(3);
        }
        return StringUtils.decapitalize(name);
    }

    public String toString(Importer importer, int indent) {
        String space = StringUtils.indent(indent);
        StringBuilder builder = new StringBuilder();

        String fieldName = getFieldName();
        String methodName = StringUtils.getSimpleName(element);
        String type = importer.onImported(element.getReturnType());

        // field
        String field = StringUtils.toDeclareField(getFieldName(), type);
        newLine(builder).append(space).append(field);

        String getter = StringUtils.toGetterMethod(methodName, fieldName, type);
        newLine(builder).append(space).append("@Override");
        newLine(builder).append(space).append(getter);
        if (Imported.AUTOWIRED) {
            newLine(builder).append(space);
            builder.append('@').append(importer.onImported(Autowired.class));
            if (!provided.required()) {
                builder.append("(required = false)");
            }
        }
        if (Imported.QUALIFIER && StringUtils.isNotBlank(provided.value())) {
            newLine(builder).append(space);
            String name = Replacer.name.replace("(\"{name}\")", provided.value());
            builder.append('@').append(importer.onImported(Qualifier.class)).append(name);
        }

        String setterName = Const.SET + getCapitalizedPropertyName();
        String setter = StringUtils.toSetterMethod(setterName, fieldName, type);
        return newLine(builder).append(space).append(setter).toString();
    }
}