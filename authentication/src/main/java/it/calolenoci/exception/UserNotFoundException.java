package it.calolenoci.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UserNotFoundException extends WebApplicationException {
    private UserNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }

    public UserNotFoundException(){
        this(ExceptionMessage.getErrorMessage("002", "User not found"));
    }
}