package com.moon.processing.decl;

import com.moon.accessor.annotation.Accessor;
import com.moon.processing.JavaDeclarable;
import com.moon.processing.JavaProvider;
import com.moon.processing.file.Java2;
import com.moon.processing.file.FileClassImpl;
import com.moon.processing.holder.*;
import com.moon.processing.util.Processing2;
import com.moon.processing.util.Imported;
import com.moon.processing.util.Test2;
import org.springframework.data.repository.Repository;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;

/**
 * 数据库访问对象
 *
 * @author benshaoye
 */
public class AccessorDeclared extends BaseHolder implements JavaProvider {

    private final Accessor accessor;
    private final TypeElement accessorElement;
    private final String accessorClassname;
    /** 数据库访问对象 */
    private final TypeDeclared typeDeclared;
    /** 实体 */
    private final TypeDeclared pojoDeclared;
    /** 数据表 */
    private final TableDeclared tableDeclared;

    private final String classname;

    private final Elements utils;
    private final Types types;

    public AccessorDeclared(
        Holders holders,
        Accessor accessor,
        TypeDeclared typeDeclared,
        TableDeclared tableDeclared,
        TypeDeclared pojoDeclared
    ) {
        super(holders);
        this.accessor = accessor;
        this.typeDeclared = typeDeclared;
        this.pojoDeclared = pojoDeclared;
        this.tableDeclared = tableDeclared;

        TypeElement accessorElem = typeDeclared.getTypeElement();
        this.classname = nameHelper().getImplClassname(accessorElem);
        this.accessorClassname = typeDeclared.getTypeClassname();
        this.accessorElement = accessorElem;
        this.utils = Processing2.getUtils();
        this.types = Processing2.getTypes();
    }

    public Accessor getAccessor() { return accessor; }

    public TypeElement getAccessorElement() { return accessorElement; }

    public String getAccessorClassname() { return accessorClassname; }

    public TypeDeclared getTypeDeclared() { return typeDeclared; }

    public TypeDeclared getPojoDeclared() { return pojoDeclared; }

    public TableDeclared getTableDeclared() { return tableDeclared; }

    public String getClassname() { return classname; }

    public Elements getUtils() { return utils; }

    public Types getTypes() { return types; }

    @Override
    public JavaDeclarable getJavaDeclare() {
        if (isJpaRepository()) {
            return JavaDeclarable.NULL;
        }
        TypeElement element = this.accessorElement;
        final ElementKind kind = element.getKind();
        if (kind == ElementKind.INTERFACE) {
            return doGetForInterface();
        }
        String error;
        if (kind == ElementKind.CLASS) {
            if (Test2.isAny(element, ABSTRACT)) {
                return doGetForAbstractClass();
            } else {
                if (Imported.isComponent(element)) {
                    return JavaDeclarable.NULL;
                } else if (Test2.isAny(element, FINAL)) {
                    error = "不能被 final 修饰";
                } else {
                    return doGetForNormalClass();
                }
            }
        } else {
            error = "应是接口";
        }
        throw new IllegalStateException("[ " + typeDeclared.getTypeClassname() + " ]" + error);
    }

    private JavaDeclarable doGetForInterface() {
        FileClassImpl classFile = newAccessorImpl();
        classFile.implement(typeDeclared.getTypeClassname());

        new AccessorOnInterface(classFile, this).doGenerate();

        return classFile;
    }

    private JavaDeclarable doGetForAbstractClass() {
        FileClassImpl classFile = newAccessorImpl();
        classFile.extend(typeDeclared.getTypeClassname());

        return classFile;
    }

    private JavaDeclarable doGetForNormalClass() {
        FileClassImpl classFile = newAccessorImpl();
        classFile.extend(typeDeclared.getTypeClassname());

        return classFile;
    }

    private FileClassImpl newAccessorImpl() {
        FileClassImpl classFile = Java2.classOf(classname);
        classFile.annotationForRepository();
        return classFile;
    }

    private boolean isJpaRepository() {
        if (Repository2.REPOSITORY_CLASSNAME != null) {
            TypeElement repository = Repository2.REPOSITORY;
            if (repository == null) {
                repository = utils.getTypeElement(Repository2.REPOSITORY_CLASSNAME);
                Repository2.REPOSITORY = repository;
            }
            if (repository == null) {
                return false;
            }
            return types.isSubtype(this.accessorElement.asType(), repository.asType());
        }
        return false;
    }

    private enum Repository2 {
        ;
        private final static String REPOSITORY_CLASSNAME;
        private static TypeElement REPOSITORY;

        static {
            String classname;
            try {
                classname = Repository.class.getCanonicalName();
            } catch (Throwable t) {
                classname = null;
            }
            REPOSITORY_CLASSNAME = classname;
        }
    }
}
