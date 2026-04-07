package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResiduoDto implements Serializable {

    // --- Identificativi ordine ---
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer rigo;
    private Integer progrGenerale;

    // --- Articolo ---
    private String fArticolo;
    private String fDescrArticolo;

    // --- Quantità ---
    private Double qtaOrdinata;
    private Double qtaBollata;
    private Double residuo;

}
