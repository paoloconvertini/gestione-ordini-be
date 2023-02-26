package it.calolenoci.dto;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.RegistroAzioni} entity
 */
@Data
public class RegistroAzioniDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer rigo;
    private String user;
    private Date data;
    private String azione;
    private Float quantita;
    private String tono;
}