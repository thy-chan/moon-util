package com.moon.processor.model;

import com.moon.mapper.annotation.IgnoreMode;
import com.moon.processor.utils.String2;

import static com.moon.mapper.annotation.IgnoreMode.*;

/**
 * @author benshaoye
 */
public class DeclareMapping {

    public final static DeclareMapping DFT = new Dft();

    private final String targetCls;
    private final String field;
    final IgnoreMode ignoreMode;
    private final String format;
    private final String defaultValue;

    public DeclareMapping(
        String targetCls, String field, String format, String defaultValue, IgnoreMode ignoreMode
    ) {
        this.field = String2.isBlank(field) ? null : field.trim();
        this.format = String2.isBlank(format) ? null : format;
        this.defaultValue = String2.isEmpty(defaultValue) ? null : defaultValue;
        this.ignoreMode = ignoreMode;
        this.targetCls = targetCls;
    }

    public boolean isIgnoreForward() {
        return ignoreMode == ALL || ignoreMode == FORWARD;
    }

    public boolean isIgnoreBackward() {
        return ignoreMode == ALL || ignoreMode == BACKWARD;
    }

    public String formatValue() { return format; }

    public String defaultValue() { return defaultValue; }

    public String getTargetCls() { return targetCls; }

    public String getField(String fromProperty) {
        return field == null ? fromProperty : field;
    }

    private static class Dft extends DeclareMapping {

        public Dft() { super(null, null, null, null, IgnoreMode.NONE); }

        @Override
        public String getField(String fromProperty) { return fromProperty; }
    }
}