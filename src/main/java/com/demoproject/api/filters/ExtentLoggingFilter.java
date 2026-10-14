package com.demoproject.api.filters;

import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.demoproject.reporting.ExtentTestManager;
import com.demoproject.reporting.HtmlEscaper;
import com.demoproject.utils.JsonUtils;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Rest Assured filter that logs each request/response (method, URL, headers, body, status,
 * time) to the framework log and to the current scenario's Extent node. Secret headers and
 * secret-looking JSON fields are masked.
 */
public class ExtentLoggingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(ExtentLoggingFilter.class.getName());
    private static final String MASK = "****";
    private static final Set<String> SECRET_HEADERS = Set.of(
            "authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key", "api-key");
    private static final Pattern SECRET_JSON_FIELD = Pattern.compile(
            "(\"[^\"]*(?:password|passwd|secret|token|apikey|api_key)[^\"]*\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        long start = System.nanoTime();
        Response response = context.next(request, responseSpec);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        Object rawBody = request.getBody();
        String requestBody = maskBody(rawBody == null ? "" : rawBody.toString());
        String responseBody = maskBody(response.asString());
        String summary = request.getMethod() + " " + request.getURI() + " -> " + response.getStatusCode()
                + " (" + elapsedMs + " ms)";
        LOG.info(summary);

        ExtentTestManager.currentTest().ifPresent(test -> {
            ExtentColor color = response.getStatusCode() < 400 ? ExtentColor.TEAL : ExtentColor.RED;
            test.info(MarkupHelper.createLabel(summary, color));
            test.info("<details><summary>Request</summary>"
                    + HtmlEscaper.pre("Headers:\n" + formatHeaders(request.getHeaders())
                    + (requestBody.isEmpty() ? "" : "\n\nBody:\n" + JsonUtils.prettyPrint(requestBody)))
                    + "</details>");
            test.info("<details><summary>Response</summary>"
                    + HtmlEscaper.pre("Status: " + response.getStatusLine() + "\nHeaders:\n"
                    + formatHeaders(response.getHeaders())
                    + (responseBody.isEmpty() ? "" : "\n\nBody:\n" + JsonUtils.prettyPrint(responseBody)))
                    + "</details>");
        });
        return response;
    }

    private static String formatHeaders(Headers headers) {
        return headers.asList().stream()
                .map(ExtentLoggingFilter::formatHeader)
                .collect(Collectors.joining("\n"));
    }

    private static String formatHeader(Header header) {
        boolean secret = SECRET_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT));
        return "  " + header.getName() + ": " + (secret ? MASK : header.getValue());
    }

    private static String maskBody(String body) {
        return SECRET_JSON_FIELD.matcher(body).replaceAll("$1\"" + MASK + "\"");
    }
}
