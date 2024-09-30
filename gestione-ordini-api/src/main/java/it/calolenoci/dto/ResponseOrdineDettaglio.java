package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class ResponseOrdineDettaglio {
    private  Integer anno;
    private  String serie;
    private  Integer progressivo;
    private String intestazione;
    private String riferimento;
    private String sottoConto;
    private String telefono;
    private String cellulare;
    private String userLock;
    private Boolean locked;
    private Double totale;
    private Date dataOrdine;
    private String modalitaPagamento;
    private String noteLogistica;
    private String userNoteLogistica;
    private LocalDateTime dataNoteLogistica;
    private List<OrdineDettaglioDto> articoli;

}
