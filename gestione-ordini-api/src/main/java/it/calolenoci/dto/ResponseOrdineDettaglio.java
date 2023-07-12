package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class ResponseOrdineDettaglio {
    private String intestazione;
    private String riferimento;
    private String sottoConto;
    private String telefono;
    private String cellulare;
    private String userLock;
    private Boolean locked;
    private Double totale;
    private List<OrdineDettaglioDto> articoli;

}
