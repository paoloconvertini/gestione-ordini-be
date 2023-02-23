package it.calolenoci.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class EmailDto {

    private String to;

    private Integer anno;

    private Integer progressivo;

    private String serie;
}
