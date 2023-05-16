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
public class ResponseOAFDettaglioDTO {
    private String intestazione;
    private String sottoConto;
    private Double totale;
    private List<OrdineFornitoreDettaglioDto> articoli;

}
