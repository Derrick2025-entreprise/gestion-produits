package com.eadl.gestionproduits.exception;

import com.eadl.gestionproduits.dto.ApiError;
import com.eadl.gestionproduits.dto.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INVALID_DATA = "Données invalides";
    private static final String INVALID_PARAMETERS = "Paramètres de requête invalides";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("{} sur {} : {}", ex.getCode(), req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        log.warn("{} sur {} : {}", ex.getCode(), req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(InvalidParameterException ex, HttpServletRequest req) {
        log.warn("{} sur {} : {}", ex.getCode(), req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldViolation(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, INVALID_DATA, req, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest req) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(lastNode(v), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER, INVALID_PARAMETERS, req, violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER, INVALID_PARAMETERS, req, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.MALFORMED_JSON,
                "Corps JSON mal formé ou type de champ incorrect", req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.TYPE_MISMATCH,
                "Valeur invalide pour le paramètre " + ex.getName(), req, List.of());
    }


    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleFrameworkError(Exception ex, HttpServletRequest req) {
        HttpStatusCode status = ((ErrorResponse) ex).getStatusCode();
        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        return build(httpStatus, httpStatus.name(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                "Une erreur interne est survenue", req, List.of());
    }

    private static String lastNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                                  HttpServletRequest req, List<FieldViolation> violations) {
        ApiError body = new ApiError(Instant.now(), status.value(), code, message, req.getRequestURI(), violations);
        return ResponseEntity.status(status).body(body);
    }
}
