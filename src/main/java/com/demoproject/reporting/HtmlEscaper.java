package com.demoproject.reporting;

/**
 * Minimal HTML escaping for text logged into the Extent report (which renders log entries as HTML).
 */
public final class HtmlEscaper {

    private HtmlEscaper() {
    }

    /**
     * @param text raw text (null-safe)
     * @return text safe to embed in HTML
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * @param text raw multi-line text
     * @return escaped text wrapped in a {@code <pre>} block
     */
    public static String pre(String text) {
        return "<pre style=\"white-space:pre-wrap;word-break:break-word;\">" + escape(text) + "</pre>";
    }
}
