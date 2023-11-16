package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
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
}
