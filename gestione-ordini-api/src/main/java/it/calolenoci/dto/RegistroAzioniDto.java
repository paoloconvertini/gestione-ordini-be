package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.RegistroAzioni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link RegistroAzioni} entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class RegistroAzioniDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer rigo;
    private String username;
    private Date createDate;
    private String azione;
    private Double quantita;
    private String tono;
    private Double qtaRiservata;
    private Double qtaProntoConsegna;
}