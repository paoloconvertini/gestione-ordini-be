package it.calolenoci.dto;

import lombok.Data;

@Data
public class AssenzaGiornoDto {

    private Long id;

    private String tipo;

    private String venditoriLabel;

    private String fasciaOraria;

    private String note;
}
