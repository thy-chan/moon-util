package com.moon.more.web;

import javax.servlet.http.HttpServletRequest;

import static com.moon.core.lang.StringUtil.isEmpty;
import static com.moon.core.lang.ThrowUtil.noInstanceError;
import static com.moon.more.web.RequestUtil.header;

/**
 * @author benshaoye
 */
public final class ContentTypeUtil {

    private ContentTypeUtil() { noInstanceError(); }

    public static boolean isAcceptAny(HttpServletRequest request) { return isAcceptOf(request, "*/*"); }

    public static boolean isAcceptJson(HttpServletRequest request) { return isAcceptOf(request, "application/json"); }

    public static boolean isAcceptHtml(HttpServletRequest request) { return isAcceptOf(request, "text/html"); }

    @SuppressWarnings("all")
    public static boolean isAcceptOf(HttpServletRequest request, String expected) {
        if (request == null || isEmpty(expected)) { return false; }
        String acceptVal = header(request, "accept");
        if (isEmpty(acceptVal)) { acceptVal = header(request, "Accept"); }
        if (isEmpty(acceptVal)) { acceptVal = header(request, "ACCEPT"); }
        return isAcceptOf(acceptVal, expected);
    }

    private static boolean isAcceptOf(String acceptVal, String expected) { return acceptVal.contains(expected); }
}