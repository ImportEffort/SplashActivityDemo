package com.example.wsj.splashdemo.http.interceptor;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.platform.Platform.INFO;


/**
 * Created by wangshijia on 2017/2/4 下午4:02.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public class LoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /** * No logs. */
        NONE,
        /** * Logs request and response lines. * <p> * Example: * <pre>{@code * --> POST /greeting HTTP/1.1 (3-byte body) * * <-- HTTP/1.1 200 OK (22ms, 6-byte body) * }</pre> */
        BASIC,
        /** * Logs request and response lines and their respective headers. * <p> * Example: * <pre>{@code * --> POST /greeting HTTP/1.1 * Host: example.com * Content-Type: plain/text * Content-Length: 3 * --> END POST * * <-- HTTP/1.1 200 OK (22ms) * Content-Type: plain/text * Content-Length: 6 * <-- END HTTP * }</pre> */
        HEADERS,
        /** * Logs request and response lines and their respective headers and bodies (if present). * <p> * Example: * <pre>{@code * --> POST /greeting HTTP/1.1 * Host: example.com * Content-Type: plain/text * Content-Length: 3 * * Hi? * --> END GET * * <-- HTTP/1.1 200 OK (22ms) * Content-Type: plain/text * Content-Length: 6 * * Hello! * <-- END HTTP * }</pre> */
        BODY
    }

    public interface Logger {
        void log(String message);

        /** * A {@link Logger} defaults output appropriate for the current platform. */

        Logger DEFAULT = new Logger() {
            @Override public void log(String message) {
                Platform.get().log(INFO, message, null);
            }
        };
    }

    public LoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    public LoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    private final Logger logger;

    private volatile Level level = Level.BODY;

    /** * Change the level at which this interceptor logs. */
    public LoggingInterceptor setLevel(Level level) {
        if (level == null)
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        String requestStartMessage = request.method() + ' ' + request.url();
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.log("----------------request start----------------");
        logger.log(requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor.
                // Forcethem to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logger.log("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (skipHeader(name)) {
                    logger.log(name + ": " + headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
            } else if (bodyEncoded(request.headers())) {
            } else if (request.body() instanceof MultipartBody) {
                logger.log("content: " + "too many bytes, ignored");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    contentType.charset(UTF8);
                }
                logger.log("content: " + buffer.readString(charset));
            }
            logger.log("----------------request end------------------");
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
            logger.log("----------------response start---------------");
        } catch (Exception e) {
            logger.log("HTTP FAILED: " + e);
            logger.log("----------------response end-----------------");
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        logger.log(protocol(response.protocol()) + " " + response.request().url());
        logger.log("info: " + "code:" + response.code() + " " + "result:" + response.message() +
                " times:" + tookMs + "ms");

        if (logHeaders) {

            if (!logBody || !HttpHeaders.hasBody(response)) {
            } else if (bodyEncoded(response.headers())) {
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8);
                    } catch (UnsupportedCharsetException e) {
                        logger.log("");
                        logger.log("Couldn't decode the response body; charset is likely " +
                                "malformed.");
                        logger.log("----------------response end-----------------");
                        return response;
                    }
                }

                if (!isPlaintext(buffer)) {
                    logger.log("");
                    logger.log("----------------response end-----------------");
                    return response;
                }

                if (contentLength != 0) {
                    logger.log("");
                    logger.log("content: " + buffer.clone().readString(charset));
                }
            }
        }
        logger.log("----------------response end-----------------");
        return response;
    }

    private boolean skipHeader(String name) {
        return !"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase
                (name) && !"Host".equalsIgnoreCase(name) && !"Accept-Encoding"
                .equalsIgnoreCase(name) && !"User-Agent".equalsIgnoreCase(name) &&
                !"Connection".equalsIgnoreCase(name);
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    private static String protocol(Protocol protocol) {
        return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
    }

    /** * Returns true if the body in question probably contains human readable text. Uses a small * sample * of code points to detect unicode control characters commonly used in binary file signatures. */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
