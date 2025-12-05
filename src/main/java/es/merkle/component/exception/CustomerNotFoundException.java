package es.merkle.component.exception;

public class CustomerNotFoundException extends PopulatorException {

    public CustomerNotFoundException(String customerId) {
        super(customerId);
    }
}
