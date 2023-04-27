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
    private String sottoConto;
    private String userLock;
    private Boolean locked;
    private List<OrdineDettaglioDto> articoli;

}
