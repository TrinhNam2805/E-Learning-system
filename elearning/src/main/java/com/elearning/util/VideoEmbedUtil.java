package com.elearning.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Chuẩn hóa URL video thành src iframe (YouTube / Vimeo / file trực tiếp). */
public final class VideoEmbedUtil {

    private static final Pattern YOUTU_BE = Pattern.compile("youtu\\.be/([^?&#/]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VIMEO = Pattern.compile("vimeo\\.com/(?:video/)?(\\d+)", Pattern.CASE_INSENSITIVE);

    private VideoEmbedUtil() {}

    /**
     * @return URL dùng cho {@code iframe src}, hoặc null nếu không nhận dạng được.
     */
    public static String embedUrl(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String url = raw.trim();

        if (url.contains("youtube.com/embed/")) {
            return sanitizeYoutubeEmbed(url);
        }
        if (url.contains("youtube.com/watch") || url.contains("youtube.com/shorts/")) {
            String v = extractQueryParam(url, "v");
            if (v == null && url.contains("/shorts/")) {
                int i = url.indexOf("/shorts/");
                String rest = url.substring(i + "/shorts/".length());
                int end = rest.indexOf('?');
                v = end > 0 ? rest.substring(0, end) : rest.replace("/", "");
            }
            return v != null ? "https://www.youtube.com/embed/" + v : null;
        }
        Matcher m = YOUTU_BE.matcher(url);
        if (m.find()) {
            return "https://www.youtube.com/embed/" + m.group(1);
        }

        Matcher vm = VIMEO.matcher(url);
        if (vm.find()) {
            return "https://player.vimeo.com/video/" + vm.group(1);
        }

        if (url.matches("(?i)https?://.+\\.(mp4|webm)(\\?.*)?")) {
            return url;
        }
        return null;
    }

    private static String sanitizeYoutubeEmbed(String url) {
        int q = url.indexOf('?');
        return q > 0 ? url.substring(0, q) : url;
    }

    private static String extractQueryParam(String url, String name) {
        try {
            String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
            for (String part : query.split("&")) {
                int eq = part.indexOf('=');
                if (eq > 0) {
                    String key = URLDecoder.decode(part.substring(0, eq), "UTF-8");
                    if (name.equals(key)) {
                        return URLDecoder.decode(part.substring(eq + 1), "UTF-8");
                    }
                }
            }
        } catch (UnsupportedEncodingException ignored) {
            /* fall through */
        }
        return null;
    }

    public static boolean isDirectVideoFile(String embedUrl) {
        return embedUrl != null && embedUrl.matches("(?i)https?://.+\\.(mp4|webm)(\\?.*)?");
    }
}
