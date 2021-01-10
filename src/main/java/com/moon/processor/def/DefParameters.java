package com.moon.processor.def;

import java.util.LinkedHashMap;

/**
 * @author benshaoye
 */
public class DefParameters extends LinkedHashMap<String, String> {

    private DefParameters() { }

    public static DefParameters empty() { return DefParameters.of(); }

    public static DefParameters of() { return new DefParameters(); }

    public static DefParameters of(String name, String type) { return of().add(name, type); }

    public DefParameters add(String name, String type) {
        put(name, type);
        return this;
    }
}