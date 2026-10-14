package com.demoproject.utils;

import com.demoproject.exceptions.FrameworkException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Single, shared Jackson {@link ObjectMapper} (thread-safe once configured) plus helpers.
 * Unknown JSON properties are ignored and null fields are not serialised.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    private JsonUtils() {
    }

    /**
     * @return the shared mapper (also registered with Rest Assured)
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * @param value object to serialise
     * @return compact JSON
     */
    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new FrameworkException("Unable to serialise " + value.getClass().getSimpleName() + " to JSON", e);
        }
    }

    /**
     * @param json JSON text
     * @param type target class
     * @param <T>  target type
     * @return deserialised object
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new FrameworkException("Unable to deserialise JSON into " + type.getSimpleName() + ": "
                    + e.getOriginalMessage(), e);
        }
    }

    /**
     * @param json JSON text
     * @param type target type reference, e.g. {@code new TypeReference<List<Post>>() {}}
     * @param <T>  target type
     * @return deserialised object
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new FrameworkException("Unable to deserialise JSON into " + type.getType() + ": "
                    + e.getOriginalMessage(), e);
        }
    }

    /**
     * @param text possibly-JSON text
     * @return pretty-printed JSON, or the original text if it is not JSON
     */
    public static String prettyPrint(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        try {
            JsonNode node = MAPPER.readTree(text);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return text;
        }
    }

    /**
     * Escapes a value for safe insertion inside a JSON string literal (quotes not included).
     *
     * @param value raw value
     * @return escaped value
     */
    public static String escape(String value) {
        String quoted = toJson(value);
        return quoted.substring(1, quoted.length() - 1);
    }
}
