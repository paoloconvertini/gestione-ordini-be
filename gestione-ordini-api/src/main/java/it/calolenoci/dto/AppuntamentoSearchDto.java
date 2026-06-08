package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppuntamentoSearchDto implements Serializable {

    private Long id;

    private Long sedeId;
    private String sedeDescrizione;

    private Long motivoId;
    private String motivoDescrizione;

    private Long motivoParentId;
    private String motivoParentDescrizione;

    private String nomeCliente;
    private String telefono;

    private String comune;

    private LocalDate dataAppuntamento;
    private LocalDateTime dataOraDa;

    private LocalDateTime dataOraA;
    private LocalTime oraDa;
    private LocalTime oraA;

    private String note;

    private String colore;

    private String tooltip;

    private String clienteLabel;

    private String indirizzoLabel;

    private String dataOraLabel;

    private String tipoEvento;

    private String descrizione;

    private List<String> codVenditori;

    private String venditoriLabel;

    private String motivoLabel;
}