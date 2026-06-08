package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AssenzaSearchDto implements Serializable {

    private Long id;

    private String tipo;

    private String venditoriLabel;

    private String periodoLabel;

    private String note;
}