package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RegisterForReflection
public class StoricoPrezzoArticoloDto {
    private LocalDateTime dataModifica;
    private BigDecimal prezzoPrecedente;
    private BigDecimal prezzoBase;
    private String utente;
}
