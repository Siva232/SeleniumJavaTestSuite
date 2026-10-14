package com.demoproject.utils;

import com.demoproject.exceptions.FrameworkException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code ${...}} placeholders. Built-in dynamic tokens:
 * <ul>
 *     <li>{@code ${timestamp}} - yyyyMMddHHmmssSSS</li>
 *     <li>{@code ${uuid}} - random UUID</li>
 *     <li>{@code ${random.alpha:N}} - N random letters</li>
 *     <li>{@code ${random.numeric:N}} - N random digits</li>
 * </ul>
 * Any other token is treated as a reference to another key and handed to a lookup function.
 */
public final class PlaceholderResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String RANDOM_ALPHA = "random.alpha:";
    private static final String RANDOM_NUMERIC = "random.numeric:";
    private static final int MAX_RANDOM_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PlaceholderResolver() {
    }

    /**
     * @param text      text that may contain placeholders (null returns null)
     * @param keyLookup resolves non-dynamic tokens (e.g. other test-data keys)
     * @param encoder   applied to every substituted value (e.g. JSON escaping); identity for plain text
     * @return text with all placeholders replaced
     */
    public static String resolve(String text, Function<String, String> keyLookup, UnaryOperator<String> encoder) {
        if (text == null || !text.contains("${")) {
            return text;
        }
        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group(1).trim();
            String value = dynamicValue(token).orElseGet(() -> keyLookup.apply(token));
            matcher.appendReplacement(result, Matcher.quoteReplacement(encoder.apply(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * @param token placeholder body without {@code ${}}
     * @return generated value if the token is a built-in dynamic token
     */
    public static Optional<String> dynamicValue(String token) {
        if ("timestamp".equals(token)) {
            return Optional.of(LocalDateTime.now().format(TIMESTAMP));
        }
        if ("uuid".equals(token)) {
            return Optional.of(UUID.randomUUID().toString());
        }
        if (token.startsWith(RANDOM_ALPHA)) {
            return Optional.of(randomString(ALPHA, parseLength(token, RANDOM_ALPHA)));
        }
        if (token.startsWith(RANDOM_NUMERIC)) {
            return Optional.of(randomString(DIGITS, parseLength(token, RANDOM_NUMERIC)));
        }
        return Optional.empty();
    }

    private static int parseLength(String token, String prefix) {
        String raw = token.substring(prefix.length()).trim();
        try {
            int length = Integer.parseInt(raw);
            if (length < 1 || length > MAX_RANDOM_LENGTH) {
                throw new FrameworkException("Placeholder ${" + token + "} length must be 1-" + MAX_RANDOM_LENGTH);
            }
            return length;
        } catch (NumberFormatException e) {
            throw new FrameworkException("Placeholder ${" + token + "} needs a numeric length, e.g. ${"
                    + prefix + "8}");
        }
    }

    private static String randomString(String alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
