package com.moon.spring.jpa.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.moon.core.lang.StringUtil;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author moonsky
 */
@MappedSuperclass
public abstract class BaseAuditRecord extends AbstractAuditRecord<String, LocalDateTime> {

    public BaseAuditRecord() { }

    public BaseAuditRecord(AbstractAuditRecord<String, LocalDateTime> audit) { super(audit); }

    public BaseAuditRecord(String createdBy, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdBy, updatedBy, createdAt, updatedAt);
    }

    public BaseAuditRecord(
        String id, String createdBy, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt
    ) { super(id, createdBy, updatedBy, createdAt, updatedAt); }

    @Override
    @JSONField(serialize = false)
    public boolean isNew() { return StringUtil.isEmpty(getId()); }
}