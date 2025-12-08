package es.merkle.component.exception;


import lombok.Getter;

@Getter
public class PopulatorException extends OrderServiceException {

    private final String customerId;

    public PopulatorException(String customerId) {
        super("Populator exception occurred with customer id: " + customerId);
        this.customerId=customerId;
    }

}
