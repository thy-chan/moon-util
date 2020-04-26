package com.moon.spring.jpa.repository;

import com.moon.spring.jpa.annotation.SqlStatement;
import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author benshaoye
 */
public class SqlRepositoryQuery implements RepositoryQuery {

    private final RepositoryMetadata metadata;
    private final ProjectionFactory factory;
    private final QueryExtractor extractor;
    private final EntityManager em;
    private final QueryMethod queryMethod;
    private final Method method;
    private final SqlStatement sql;

    public SqlRepositoryQuery(
        SqlStatement sql,
        Method method,
        RepositoryMetadata metadata,
        ProjectionFactory factory,
        QueryExtractor extractor,
        EntityManager em
    ) {
        this.metadata = metadata;
        this.factory = factory;
        this.method = method;
        this.extractor = extractor;
        this.em = em;
        this.sql = Objects.requireNonNull(sql);
        this.queryMethod = new QueryMethod(method, metadata, factory);

        Class returnCls = method.getReturnType();
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) returnType;
            Type rawType = paramType.getRawType();
            Type ownerType = paramType.getOwnerType();
            Type[] argsType = paramType.getActualTypeArguments();

        }

        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
        }
    }

    @Override
    public Object execute(Object[] parameters) {
        Query query = em.createNativeQuery("SELECT * FROM `tb_user_part_details`");
        if (query instanceof NativeQuery) {
            NativeQuery nativeQuery = (NativeQuery) query;
            // nativeQuery.
            List list = query.getResultList();
            return list;
        }else {
            List list = query.getResultList();
            return list;
        }
    }

    @Override
    public QueryMethod getQueryMethod() { return queryMethod; }
}