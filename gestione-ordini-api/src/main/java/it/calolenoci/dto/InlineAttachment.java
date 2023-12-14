package it.calolenoci.dto;

import lombok.*;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InlineAttachment extends AbstractAttachment{

    private String contentId;

    public InlineAttachment(String name, File file, String contentType, String contentId) {
        super(name, file, contentType);
        this.contentId = contentId;
    }
}
