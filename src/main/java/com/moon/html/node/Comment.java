package com.moon.html.node;

import com.moon.core.lang.StringUtil;

/**
 * @author moonsky
 */
public final class Comment extends Node {

    private Element parent;

    public Comment(String comment) {
        super("#comment", 8);
        setNodeValue(StringUtil.trimToEmpty(comment));
    }

    public int length() { return nodeValue().length(); }
}
