package com.elearning.util;

import org.springframework.web.util.HtmlUtils;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LessonContentHtmlSanitizer {

    private static final Set<String> ALLOWED_TAGS = unmodifiableSet(
            "p", "br", "hr",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "strong", "em", "b", "i", "u",
            "ul", "ol", "li",
            "blockquote", "code", "pre",
            "table", "thead", "tbody", "tr", "th", "td",
            "a", "img", "div", "span"
    );

    private static final Set<String> SELF_CLOSING_TAGS = unmodifiableSet("br", "hr", "img");
    private static final Set<String> BLOCKED_CONTENT_TAGS = unmodifiableSet(
            "script", "style", "iframe", "object", "embed", "svg", "math",
            "canvas", "noscript", "template", "textarea", "select", "option"
    );

    private LessonContentHtmlSanitizer() {}

    public static String sanitizeForRender(String rawContent) {
        String content = normalize(rawContent);
        if (content.isEmpty()) {
            return "<p>Lesson content is being updated.</p>";
        }
        if (!looksLikeHtml(content)) {
            return paragraphize(content);
        }

        SanitizingCallback callback = new SanitizingCallback();
        try {
            new ParserDelegator().parse(new StringReader(content), callback, true);
        } catch (IOException ignored) {
            return paragraphize(content);
        }

        String sanitized = callback.finish();
        if (sanitized.trim().isEmpty()) {
            return "<p>Lesson content is being updated.</p>";
        }
        if (!callback.hasMarkup()) {
            return paragraphize(callback.getPlainText());
        }
        return sanitized;
    }

    public static String sanitizeForStorage(String rawContent) {
        return sanitizeForRender(rawContent);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\u0000", "").trim();
    }

    private static boolean looksLikeHtml(String content) {
        return content.matches("(?s).*</?[a-zA-Z][^>]*>.*");
    }

    private static String paragraphize(String plainText) {
        String normalized = normalize(plainText).replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.isEmpty()) {
            return "<p>Lesson content is being updated.</p>";
        }

        String[] parts = normalized.split("\n\\s*\n");
        StringBuilder html = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (html.length() > 0) {
                html.append('\n');
            }
            html.append("<p>")
                    .append(HtmlUtils.htmlEscape(trimmed).replace("\n", "<br>"))
                    .append("</p>");
        }
        return html.length() == 0 ? "<p>Lesson content is being updated.</p>" : html.toString();
    }

    private static Set<String> unmodifiableSet(String... values) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
    }

    private static final class SanitizingCallback extends HTMLEditorKit.ParserCallback {
        private final StringBuilder html = new StringBuilder();
        private final StringBuilder plainText = new StringBuilder();
        private final Deque<String> openTags = new ArrayDeque<String>();
        private int blockedDepth = 0;
        private boolean markup = false;

        @Override
        public void handleText(char[] data, int pos) {
            if (blockedDepth > 0 || data == null || data.length == 0) {
                return;
            }
            String text = new String(data);
            plainText.append(text).append('\n');
            html.append(HtmlUtils.htmlEscape(text));
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            handleOpeningTag(tag, attributes, false);
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            handleOpeningTag(tag, attributes, true);
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            String tagName = normalizeTagName(tag);
            if (BLOCKED_CONTENT_TAGS.contains(tagName)) {
                if (blockedDepth > 0) {
                    blockedDepth--;
                }
                return;
            }
            if (blockedDepth > 0 || !ALLOWED_TAGS.contains(tagName) || SELF_CLOSING_TAGS.contains(tagName)) {
                return;
            }
            closeUntil(tagName);
        }

        String finish() {
            while (!openTags.isEmpty()) {
                html.append("</").append(openTags.pop()).append('>');
            }
            return html.toString().trim();
        }

        boolean hasMarkup() {
            return markup;
        }

        String getPlainText() {
            return plainText.toString();
        }

        private void handleOpeningTag(HTML.Tag tag, MutableAttributeSet attributes, boolean simpleTag) {
            String tagName = normalizeTagName(tag);
            if (BLOCKED_CONTENT_TAGS.contains(tagName)) {
                if (!simpleTag) {
                    blockedDepth++;
                }
                return;
            }
            if (blockedDepth > 0 || !ALLOWED_TAGS.contains(tagName)) {
                return;
            }

            markup = true;
            html.append('<').append(tagName).append(buildAttributes(tagName, attributes));
            if (simpleTag || SELF_CLOSING_TAGS.contains(tagName)) {
                html.append('>');
                return;
            }
            html.append('>');
            openTags.push(tagName);
        }

        private void closeUntil(String tagName) {
            while (!openTags.isEmpty()) {
                String current = openTags.pop();
                html.append("</").append(current).append('>');
                if (current.equals(tagName)) {
                    return;
                }
            }
        }

        private String buildAttributes(String tagName, MutableAttributeSet attributes) {
            if (attributes == null) {
                return "";
            }

            if ("a".equals(tagName)) {
                String href = sanitizeUrl(attributeValue(attributes, HTML.Attribute.HREF));
                if (href != null) {
                    return " href=\"" + HtmlUtils.htmlEscape(href) + "\" rel=\"noopener noreferrer\" target=\"_blank\"";
                }
                return "";
            }

            if ("img".equals(tagName)) {
                String src = sanitizeUrl(attributeValue(attributes, HTML.Attribute.SRC));
                if (src == null) {
                    return "";
                }
                String alt = attributeValue(attributes, HTML.Attribute.ALT);
                StringBuilder attrs = new StringBuilder();
                attrs.append(" src=\"").append(HtmlUtils.htmlEscape(src)).append('"');
                if (alt != null && !alt.isEmpty()) {
                    attrs.append(" alt=\"").append(HtmlUtils.htmlEscape(alt)).append('"');
                }
                return attrs.toString();
            }

            if ("div".equals(tagName) || "span".equals(tagName)) {
                String classValue = attributeValue(attributes, HTML.Attribute.CLASS);
                if (classValue != null && classValue.toLowerCase(Locale.ROOT).contains("material-hint")) {
                    return " class=\"material-hint\"";
                }
            }
            return "";
        }

        private String attributeValue(MutableAttributeSet attributes, HTML.Attribute attribute) {
            Object value = attributes.getAttribute(attribute);
            return value == null ? null : String.valueOf(value).trim();
        }

        private String sanitizeUrl(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            String lower = trimmed.toLowerCase(Locale.ROOT);
            if (lower.startsWith("javascript:")
                    || lower.startsWith("vbscript:")
                    || lower.startsWith("data:")
                    || lower.startsWith("//")) {
                return null;
            }
            if (lower.startsWith("http://")
                    || lower.startsWith("https://")
                    || lower.startsWith("mailto:")
                    || trimmed.startsWith("/")
                    || trimmed.startsWith("./")
                    || trimmed.startsWith("../")
                    || trimmed.startsWith("#")
                    || trimmed.indexOf(':') < 0) {
                return trimmed;
            }
            return null;
        }

        private String normalizeTagName(HTML.Tag tag) {
            return tag == null ? "" : tag.toString().toLowerCase(Locale.ROOT);
        }
    }
}
