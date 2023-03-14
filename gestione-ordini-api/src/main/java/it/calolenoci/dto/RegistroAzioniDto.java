package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.RegistroAzioni} entity
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
    private Float quantita;
    private String tono;
}