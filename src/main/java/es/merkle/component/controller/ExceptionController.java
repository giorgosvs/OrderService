package es.merkle.component.controller;

import es.merkle.component.exception.GeneralException;
import es.merkle.component.exception.InvalidOrderException;
import es.merkle.component.exception.PopulatorException;
import es.merkle.component.exception.ResourceNotFoundException;
import es.merkle.component.model.api.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    //invalid order type 400
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOrderException(InvalidOrderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(),ex.getMessage()));
    }

    //entity not found 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(),ex.getMessage()));
    }

    //internal server error 500
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiErrorResponse> handleInternalServerError(GeneralException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    //populator error 404
    @ExceptionHandler(PopulatorException.class)
    public ResponseEntity<ApiErrorResponse> handlePopulatorException(PopulatorException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    //Illegal argument exception
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    //Json parse error
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonParse(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "JSON parse error"));
    }
 }
