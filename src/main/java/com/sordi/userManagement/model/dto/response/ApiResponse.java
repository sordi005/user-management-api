package com.sordi.userManagement.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all REST endpoints.
 * Provides consistent response format across the entire application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the operation was successful
     */
    private boolean success;

    /**
     * Human-readable message describing the result
     */
    private String message;

    /**
     * The actual data payload (null for errors)
     */
    private T data;

    /**
     * HTTP status code
     */
    private int statusCode;

    /**
     * Timestamp when the response was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    /**
     * Error details (only present when success = false)
     */
    private String error;

    // Static factory methods for convenience

    /**
     * Creates a successful response with data
     */
    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return new ApiResponse<>(
            true,
            message,
            data,
            statusCode,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * Creates a successful response with data and default message
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully", 200);
    }

    /**
     * Creates a successful response without data
     */
    public static <T> ApiResponse<T> success(String message, int statusCode) {
        return new ApiResponse<>(
            true,
            message,
            null,
            statusCode,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * Creates an error response
     */
    public static <T> ApiResponse<T> error(String message, String error, int statusCode) {
        return new ApiResponse<>(
            false,
            message,
            null,
            statusCode,
            LocalDateTime.now(),
            error
        );
    }

    /**
     * Creates an error response with default message
     */
    public static <T> ApiResponse<T> error(String error, int statusCode) {
        return error("Operation failed", error, statusCode);
    }
}
