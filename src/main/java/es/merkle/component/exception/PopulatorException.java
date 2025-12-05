package es.merkle.component.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PopulatorException extends OrderServiceException {

    private String customerId;

    @Override
    public String getMessage() {
        return "Populator exception occurred with customer id: " + customerId;
    }
}
