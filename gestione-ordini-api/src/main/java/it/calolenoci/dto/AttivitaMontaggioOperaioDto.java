package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttivitaMontaggioOperaioDto implements Serializable {

    private Long id;

    private Long idOperaio;

    private String nomeOperaio;

    private Boolean principale;
}
