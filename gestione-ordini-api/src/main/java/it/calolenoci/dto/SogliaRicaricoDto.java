package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import java.math.BigDecimal;

@Data
@RegisterForReflection
public class SogliaRicaricoDto {
    private String classeA1;
    private BigDecimal percentuale;
    private BigDecimal ricaricoImpostato;
}
