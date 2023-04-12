package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InlineAttachment extends AbstractAttachment{

    private String contentId;

    public InlineAttachment(String name, File file, String contentType, String contentId) {
        super(name, file, contentType);
        this.contentId = contentId;
    }
}
