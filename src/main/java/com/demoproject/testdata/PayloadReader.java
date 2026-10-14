package com.demoproject.testdata;

import com.demoproject.exceptions.FrameworkException;
import com.demoproject.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads API request bodies from {@code testdata/payloads/*.json}, resolves {@code ${...}}
 * placeholders from {@code testdata.properties} (JSON-escaped) and maps them to POJOs with Jackson.
 */
public final class PayloadReader {

    private static final String PAYLOAD_DIR = "testdata/payloads/";

    private PayloadReader() {
    }

    /**
     * @param fileName payload file name, e.g. {@code create_post.json}
     * @param type     POJO type
     * @param <T>      POJO type
     * @return deserialised, placeholder-resolved payload
     */
    public static <T> T read(String fileName, Class<T> type) {
        return JsonUtils.fromJson(readResolved(fileName), type);
    }

    /**
     * @param fileName payload file name
     * @return JSON text with placeholders resolved
     */
    public static String readResolved(String fileName) {
        String raw = readRaw(fileName);
        return TestDataReader.getInstance().resolvePlaceholders(raw, JsonUtils::escape);
    }

    private static String readRaw(String fileName) {
        String resource = PAYLOAD_DIR + fileName;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new FrameworkException("Payload '" + fileName + "' not found under src/test/resources/"
                        + PAYLOAD_DIR);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read payload '" + resource + "'", e);
        }
    }
}
