package com.example.fx.infrastructure.web;

import com.example.fx.domain.exception.AccountNotFoundException;
import com.example.fx.domain.exception.InsufficientFundsException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemHandler {

  @ExceptionHandler(InsufficientFundsException.class)
  ProblemDetail handleInsufficient(InsufficientFundsException ex, HttpServletRequest req) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setType(URI.create("https://example.com/problems/insufficient-funds"));
    problemDetail.setTitle("Insufficient funds");
    problemDetail.setInstance(URI.create(req.getRequestURI()));
    return problemDetail;
  }

  @ExceptionHandler(AccountNotFoundException.class)
  ProblemDetail handleNotFound(AccountNotFoundException ex, HttpServletRequest req) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setType(URI.create("https://example.com/problems/account-not-found"));
    problemDetail.setTitle("Account not found");
    problemDetail.setInstance(URI.create(req.getRequestURI()));
    return problemDetail;
  }

  @ExceptionHandler(ErrorResponseException.class)
  ProblemDetail handleSpring(ErrorResponseException ex) {
    return ex.getBody();
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleOther(Exception ex, HttpServletRequest req) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    problemDetail.setType(URI.create("https://example.com/problems/internal-error"));
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setInstance(URI.create(req.getRequestURI()));
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setTitle("Bad Request");
    problemDetail.setType(URI.create("https://example.com/problems/validation-error"));
    problemDetail.setInstance(URI.create(req.getRequestURI()));

    List<ValidationError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());

    problemDetail.setProperty("errors", errors);
    return problemDetail;
  }

  public record ValidationError(String field, String message) {}
}
