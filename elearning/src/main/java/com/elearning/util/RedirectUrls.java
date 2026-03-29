package com.elearning.util;

/**
 * Validates in-app redirect targets (open redirect protection).
 */
public final class RedirectUrls {

    private RedirectUrls() {}

    public static boolean isSafeRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (!path.startsWith("/")) {
            return false;
        }
        if (path.startsWith("//")) {
            return false;
        }
        if (path.contains("..") || path.contains("\\")) {
            return false;
        }
        return !path.contains("://");
    }
}
