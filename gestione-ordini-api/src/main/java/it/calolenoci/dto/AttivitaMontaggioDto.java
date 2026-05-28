package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttivitaMontaggioDto implements Serializable {

    private Long id;
    private String tipoAppuntamento;
    // Ordine cliente
    private Integer ordineAnno;
    private String ordineSerie;
    private Integer ordineProgressivo;

    private String numeroOrdine;

    // Date
    private LocalDateTime dataOraDa;
    private LocalDateTime dataOraA;

    // Cliente
    private String nomeCliente;
    private String telefono;
    private String email;

    // Indirizzo
    private String via;
    private String civico;
    private String cap;
    private String comune;
    private String provincia;

    // Operatività
    private Boolean scalaMobile;

    // Stato
    private String stato;

    // Reminder
    private Boolean promemoriaInviato;
    private LocalDateTime dataInvioPromemoria;

    // Completamento
    private LocalDateTime dataCompletamento;

    // Note
    private String note;

    // Checklist attività
    private List<AttivitaMontaggioDettDto> dettagli;

    // Operai
    private List<AttivitaMontaggioOperaioDto> operai;
}