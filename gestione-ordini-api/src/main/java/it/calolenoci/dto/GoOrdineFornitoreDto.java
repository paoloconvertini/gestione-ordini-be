package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.GoOrdineFornitore} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoOrdineFornitoreDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Boolean flInviato;
    private String note;
    private Date dataInvio;
}