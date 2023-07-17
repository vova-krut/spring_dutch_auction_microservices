package eu.attempto.dutch_auction_microservices.backend_service.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public HttpErrorResponse handleBadRequest(BadRequestException ex) {
    return HttpErrorResponse.builder()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .message(ex.getMessage())
        .build();
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(InternalServerException.class)
  public HttpErrorResponse httpInternalError(InternalServerException ex) {
    return HttpErrorResponse.builder()
        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message(ex.getMessage())
        .build();
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ValidationErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
    return ValidationErrorResponse.builder()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .errors(getFormattedErrors(ex))
        .build();
  }

  private Map<String, String> getFormattedErrors(MethodArgumentNotValidException ex) {
    var errors = new HashMap<String, String>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return errors;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ValidationErrorResponse handleConstraintViolationException(
      ConstraintViolationException ex) {
    return ValidationErrorResponse.builder()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .errors(getFormattedErrors(ex))
        .build();
  }

  private Map<String, String> getFormattedErrors(ConstraintViolationException ex) {
    var errors = new HashMap<String, String>();
    ex.getConstraintViolations()
        .forEach(
            error -> {
              var fieldName = error.getPropertyPath().toString();
              var errorMessage = error.getMessage();
              errors.put(fieldName, errorMessage);
            });
    return errors;
  }
}
