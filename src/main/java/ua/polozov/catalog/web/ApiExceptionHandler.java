package ua.polozov.catalog.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(IllegalArgumentException ex) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("type", URI.create("about:blank"));
        problem.put("title", "Conflict");
        problem.put("status", 409);
        problem.put("detail", ex.getMessage());
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(409).body(problem);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("type", URI.create("about:blank"));
        problem.put("title", "Not Found");
        problem.put("status", 404);
        String detail = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        problem.put("detail", detail);
        log.warn("Not found: {}", detail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("type", URI.create("about:blank"));
        problem.put("title", "Not Found");
        problem.put("status", 404);
        String detail = ex.getMessage() != null ? ex.getMessage() : "No static resource found";
        problem.put("detail", detail);
        log.warn("Static resource not found: {}", detail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("type", URI.create("about:blank"));
        problem.put("title", "Validation Failed");
        problem.put("status", 400);
        problem.put("detail", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        log.warn("Validation failed: {}", ex.getBindingResult().getAllErrors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("type", URI.create("about:blank"));
        problem.put("title", "Internal Server Error");
        problem.put("status", 500);
        String detail = ex.getMessage();
        if (ex.getCause() != null) detail = ex.getCause().toString();
        problem.put("detail", detail);
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
