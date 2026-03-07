package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ProgrammaConsegnaDto implements Serializable {

    private Integer anno;
    private String serie;
    private Integer progressivo;

    private Integer veicolo;

    private LocalDate dataConsegna;

    private Character oraConsegna;

    private Long ordine;

}