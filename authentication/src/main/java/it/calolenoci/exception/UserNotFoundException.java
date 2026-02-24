package it.calolenoci.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class UserNotFoundException extends WebApplicationException {
    private UserNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }

    public UserNotFoundException(){
        this(ExceptionMessage.getErrorMessage("002", "User not found"));
    }
}