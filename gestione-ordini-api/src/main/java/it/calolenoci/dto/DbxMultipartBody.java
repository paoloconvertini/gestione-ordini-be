package it.calolenoci.dto;


import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public class DbxMultipartBody {

    @FormParam("file")
    @PartType(MediaType.TEXT_PLAIN)
    public String file;

    @FormParam("folder")
    @PartType(MediaType.TEXT_PLAIN)
    public String folder;

    @FormParam("folder")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;


}
