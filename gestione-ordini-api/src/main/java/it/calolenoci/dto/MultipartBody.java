package it.calolenoci.dto;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;

public class MultipartBody {

    @FormParam("file")
    @PartType(MediaType.TEXT_PLAIN)
    public String file;

    @FormParam("orderId")
    @PartType(MediaType.TEXT_PLAIN)
    public String orderId;

}
