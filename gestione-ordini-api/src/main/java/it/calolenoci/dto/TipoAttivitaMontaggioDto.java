package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TipoAttivitaMontaggioDto implements Serializable {


    private Long id;

    private String descrizione;

    private Integer ordineVisualizzazione;

    private Boolean attivo;
}
