package it.calolenoci.dto;

import java.io.File;

public class MailAttachment extends AbstractAttachment{
    public MailAttachment(String name, File file, String contentType) {
        super(name, file, contentType);
    }
}
