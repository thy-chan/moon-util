package com.moon.core.util.support;

import com.moon.core.io.FileUtil;
import com.moon.core.lang.support.SystemSupport;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.moon.core.lang.ThrowUtil.noInstanceError;

/**
 * @author benshaoye
 */
public final class ResourceSupport {

    static final char SEP = '/';
    static final String ROOT = "src/main/resource";

    private ResourceSupport() {
        noInstanceError();
    }

    public static boolean resourceExists(String path) {
        if (path == null) {
            return false;
        } else {
            path = FileUtil.formatPath(path);
            URL url = getUrl(path);
            if (url == null && ((path = path.trim()).length() > 0)) {
                if (getUrl(path) == null) {
                    if (getUrl(resetPath(path)) == null) {
                        if (new File(path).exists() || new File(resetPath(path)).exists()) {
                            return true;
                        } else {
                            return new File(ROOT, path).exists();
                        }
                    } else {
                        return true;
                    }
                }
            }
            return true;
        }
    }

    private static String resetPath(String path) {
        return path.charAt(0) == SEP ? path.substring(1) : SEP + path;
    }

    private static URL toUrlOrNull(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL getResourceAsURL(String path) {
        if (path == null) {
            return null;
        } else {
            path = FileUtil.formatPath(path);
            URL url = getUrl(path);
            if (url == null && ((path = path.trim()).length() > 0)) {
                if ((url = getUrl(path)) == null) {
                    if ((url = getUrl(resetPath(path))) == null) {
                        File file = new File(path);
                        if (file.exists()) {
                            return toUrlOrNull(file);
                        } else if ((file = new File(resetPath(path))).exists()) {
                            return toUrlOrNull(file);
                        } else if ((file = new File(ROOT, path)).exists()) {
                            return toUrlOrNull(file);
                        }
                    }
                }
            }
            return url;
        }
    }

    static URL getUrl(String path) {
        return SystemSupport.class.getResource(path);
    }

    public static InputStream getResourceAsStream(String path) {
        try {
            return getResourceAsURL(path).openStream();
        } catch (Exception e) {
            throw new IllegalArgumentException(path, e);
        }
    }

    public static InputStream getResourceAsStreamOrNull(String path) {
        try {
            return getResourceAsURL(path).openStream();
        } catch (Exception e) {
            return null;
        }
    }
}
