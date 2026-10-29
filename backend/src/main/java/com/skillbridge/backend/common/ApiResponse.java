package com.skillbridge.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String correlationId,
    T data,
    PaginationMeta meta,
    ApiError error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, UUID.randomUUID().toString(), data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, PaginationMeta meta) {
        return new ApiResponse<>(true, UUID.randomUUID().toString(), data, meta, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, List<FieldIssue> details) {
        return new ApiResponse<>(
            false,
            UUID.randomUUID().toString(),
            null,
            null,
            new ApiError(code, message, details, Instant.now().toString())
        );
    }

    public record PaginationMeta(int page, int pageSize, long totalCount) {}

    public record ApiError(
        String code,
        String message,
        List<FieldIssue> details,
        String timestamp
    ) {}

    public record FieldIssue(String field, String issue) {}
}
