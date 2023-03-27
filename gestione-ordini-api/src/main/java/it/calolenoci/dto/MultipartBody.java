package it.calolenoci.dto;


import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public class MultipartBody {

    @FormParam("file")
    @PartType(MediaType.TEXT_PLAIN)
    public String file;

    @FormParam("orderId")
    @PartType(MediaType.TEXT_PLAIN)
    public String orderId;

}
