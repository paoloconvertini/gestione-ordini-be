package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
public class PrimanotaDto implements Serializable {
    LocalDate datamovimento;
    String numerodocumento;
    String causale;
    Integer gruppoconto;
    String sottoconto;
    String descrsuppl;
    Double importo;
    Integer progrgenerale;
    Integer protocollo;
    Integer anno;
    String giornale;
    Integer progrprimanota;
}
