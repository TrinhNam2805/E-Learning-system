package com.elearning.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Builds short preview lines from lesson HTML (headings, list items, or first paragraph),
 * similar to curriculum previews on large course platforms.
 */
public final class LessonContentOutlineExtractor {

    private static final int MAX_LINES = 5;
    private static final int MAX_LINE_CHARS = 100;
    private static final int SNIPPET_CHARS = 140;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private LessonContentOutlineExtractor() {
    }

    public static List<String> extractLines(String html) {
        List<String> lines = new ArrayList<>();
        if (html == null || html.trim().isEmpty()) {
            return lines;
        }

        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        if (body == null) {
            return lines;
        }

        addHeadings(body, lines);
        if (lines.size() < 2) {
            addListItems(body, lines);
        }
        if (lines.isEmpty()) {
            addTextSnippet(body, lines);
        }
        return lines;
    }

    private static void addHeadings(Element body, List<String> lines) {
        Elements headings = body.select("h2, h3, h4");
        Set<String> seen = new LinkedHashSet<>();
        for (Element h : headings) {
            if (lines.size() >= MAX_LINES) {
                break;
            }
            String t = normalizeText(h.text());
            if (t.length() < 3) {
                continue;
            }
            String key = t.toLowerCase();
            if (seen.contains(key)) {
                continue;
            }
            seen.add(key);
            lines.add(truncate(t, MAX_LINE_CHARS));
        }
    }

    private static void addListItems(Element body, List<String> lines) {
        Element ul = body.selectFirst("ul");
        if (ul == null) {
            return;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (Element li : ul.select("> li")) {
            if (lines.size() >= MAX_LINES) {
                break;
            }
            String t = normalizeText(li.text());
            if (t.length() < 3) {
                continue;
            }
            String key = t.toLowerCase();
            if (seen.contains(key)) {
                continue;
            }
            seen.add(key);
            lines.add(truncate(t, MAX_LINE_CHARS));
        }
    }

    private static void addTextSnippet(Element body, List<String> lines) {
        Element p = body.selectFirst("p");
        String raw = p != null ? p.text() : body.text();
        String t = normalizeText(raw);
        if (t.length() < 20) {
            return;
        }
        lines.add(truncate(t, SNIPPET_CHARS));
    }

    private static String normalizeText(String s) {
        if (s == null) {
            return "";
        }
        return WHITESPACE.matcher(s.trim()).replaceAll(" ");
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        if (max <= 3) {
            return s.substring(0, max);
        }
        return s.substring(0, max - 1).trim() + "…";
    }
}
