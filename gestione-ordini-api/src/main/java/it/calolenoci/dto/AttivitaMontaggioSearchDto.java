package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AttivitaMontaggioSearchDto implements Serializable {

    private Long id;

    private String titolo;

    private LocalDateTime dataOraDa;
    private LocalDateTime dataOraA;

    private String nomeCliente;

    private String comune;

    private String stato;

    private Boolean scalaMobile;

    private String numeroOrdine;

    private String telefono;

    private String operai;

    private String attivita;

    private String colore;

    private String tooltip;

    private String tipoAppuntamento;

    private String clienteLabel;

    private String indirizzoLabel;

    private String attivitaLabel;

    private String dataOraLabel;
}