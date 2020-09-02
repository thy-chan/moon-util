package com.moon.data.jpa.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.moon.core.lang.StringUtil;
import com.moon.core.model.getter.IdGetter;

import javax.persistence.MappedSuperclass;

/**
 * @author moonsky
 */
@MappedSuperclass
public abstract class BaseRecordEntity extends AbstractJpaRecord<String> implements IdGetter {

    public BaseRecordEntity() { }

    public BaseRecordEntity(String id) { super(id); }

    public BaseRecordEntity(AbstractJpaRecord<String> recordable) { super(recordable); }

    @Override
    @JSONField(serialize = false)
    public boolean isNew() { return StringUtil.isEmpty(getId()); }
}
