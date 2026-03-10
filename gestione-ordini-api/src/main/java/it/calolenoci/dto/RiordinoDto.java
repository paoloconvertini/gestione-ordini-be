package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RiordinoDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Long ordine;
}
