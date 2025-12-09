package es.merkle.component.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;

//Dto for exception handling
@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String message;
}
