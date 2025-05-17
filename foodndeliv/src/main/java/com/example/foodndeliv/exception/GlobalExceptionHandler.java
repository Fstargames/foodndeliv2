package com.example.foodndeliv.exception; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Ensure 400 is returned
    @ResponseBody // Ensure the return value is written to the response body as JSON
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation exception caught by GlobalExceptionHandler for request: {}", request.getDescription(false));

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existingValue, newValue) -> existingValue + "; " + newValue // Handle multiple errors on same field
                ));

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("error", "Validation Failed");
        responseBody.put("message", "Input validation failed for one or more fields.");
        responseBody.put("fieldErrors", fieldErrors);
        responseBody.put("path", request.getDescription(false).replace("uri=", ""));

        logger.warn("Detailed Validation error: {}, Path: {}", fieldErrors, responseBody.get("path"));
        return responseBody;
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
        logger.warn("NoSuchElementException caught by GlobalExceptionHandler: {} for request: {}", ex.getMessage(), request.getDescription(false));
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("status", HttpStatus.NOT_FOUND.value());
        responseBody.put("error", "Resource Not Found");
        responseBody.put("message", ex.getMessage());
        responseBody.put("path", request.getDescription(false).replace("uri=", ""));
        return responseBody;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody // Return type is ResponseEntity, so Spring will use its status
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("IllegalArgumentException caught by GlobalExceptionHandler: {} for request: {}", ex.getMessage(), request.getDescription(false));
        HttpStatus statusToReturn = HttpStatus.BAD_REQUEST;
        String errorType = "Invalid Argument";

        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("already exists")) {
            statusToReturn = HttpStatus.CONFLICT;
            errorType = "Conflict";
        }

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("status", statusToReturn.value());
        responseBody.put("error", errorType);
        responseBody.put("message", ex.getMessage());
        responseBody.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(responseBody, statusToReturn);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleAllOtherExceptions(Exception ex, WebRequest request) {
        logger.error("Generic Exception caught by GlobalExceptionHandler: {} for request: {}", ex.getMessage(), request.getDescription(false), ex); // Log full stack trace
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseBody.put("error", "Internal Server Error");
        responseBody.put("message", "An unexpected internal error occurred. Please try again later.");
        responseBody.put("path", request.getDescription(false).replace("uri=", ""));
        return responseBody;
    }
}



