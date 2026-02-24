package it.calolenoci.dto;


import org.jboss.resteasy.reactive.PartType;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;

public class MultipartBody {

    @FormParam("file")
    @PartType(MediaType.TEXT_PLAIN)
    public String file;

    @FormParam("orderId")
    @PartType(MediaType.TEXT_PLAIN)
    public String orderId;

}
