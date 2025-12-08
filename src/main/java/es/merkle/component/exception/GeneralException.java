package es.merkle.component.exception;


//Exception for 500
public class GeneralException extends OrderServiceException{
    public GeneralException(String message) {
        super(message);
    }
}
