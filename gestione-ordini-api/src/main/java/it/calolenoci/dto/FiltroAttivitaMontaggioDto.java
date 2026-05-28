package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class FiltroAttivitaMontaggioDto implements Serializable {

    private LocalDate dataDa;

    private LocalDate dataA;

    private String nomeCliente;

    private String stato;

    private Long idOperaio;

    private Boolean scalaMobile;

    private Integer ordineAnno;

    private String ordineSerie;

    private Integer ordineProgressivo;

    private String tipoAppuntamento;
}
