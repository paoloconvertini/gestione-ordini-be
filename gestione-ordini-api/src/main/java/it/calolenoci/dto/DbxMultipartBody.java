package it.calolenoci.dto;


import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class DbxMultipartBody {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream file;

    @FormParam("folder")
    @PartType(MediaType.TEXT_PLAIN)
    public String folder;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;


}
