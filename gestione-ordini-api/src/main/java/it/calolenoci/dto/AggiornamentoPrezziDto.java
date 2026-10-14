package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@RegisterForReflection
public class AggiornamentoPrezziDto {
    private String classeA1;
    private BigDecimal percentuale;
    private BigDecimal ricaricoImpostato;
    private boolean includiBloccati;
    private boolean portaASoglia;
    private List<PrezzoArticoloDto> articoli;
    private BigDecimal percentualeDeselezionati;
    private List<PrezzoArticoloDto> articoliDeselezionati;
}
