package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppuntamentoDto implements Serializable {
    private Long id;
    private Long sedeId;
    private String sedeDescrizione;
    private Long motivoId;
    private String motivoDescrizione;
    private Long motivoParentId;
    private String motivoParentDescrizione;
    private Integer gruppoConto;
    private String sottoConto;
    private String nomeCliente;
    private String telefono;
    private String email;
    private String via;
    private String civico;
    private String cap;
    private String comune;
    private String provincia;
    private LocalDate dataAppuntamento;
    private LocalTime oraDa;
    private LocalTime oraA;
    private String tipoEvento;
    private String descrizione;
    private List<String> codVenditori;
    private String note;
    private Boolean promemoriaInviato;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
