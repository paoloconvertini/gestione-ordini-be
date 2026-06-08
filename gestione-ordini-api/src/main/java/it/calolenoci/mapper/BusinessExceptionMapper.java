package it.calolenoci.mapper;

import it.calolenoci.exception.BusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

import org.jboss.logging.Logger;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    private static final Logger log = Logger.getLogger(BusinessExceptionMapper.class);

    @Override
    public Response toResponse(BusinessException exception) {
        log.error("BusinessException: " + exception.getMessage(), exception);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", exception.getMessage()))
                .build();
    }
}