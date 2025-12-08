package es.merkle.component.exception;


public class ResourceNotFoundException extends OrderServiceException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
