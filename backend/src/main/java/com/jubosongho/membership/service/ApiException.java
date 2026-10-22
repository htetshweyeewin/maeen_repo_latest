package com.jubosongho.membership.service;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** বাংলা বার্তাসহ HTTP ত্রুটি। */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> fieldErrors;

    public ApiException(HttpStatus status, String message) {
        this(status, message, Map.of());
    }

    public ApiException(HttpStatus status, String message, Map<String, String> fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors;
    }

    public static ApiException notFound(String what) {
        return new ApiException(HttpStatus.NOT_FOUND, what + " পাওয়া যায়নি");
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException field(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, Map.of(field, message));
    }

    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "এই তথ্য দেখার/পরিবর্তনের অনুমতি আপনার নেই");
    }

    public HttpStatus getStatus() { return status; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
