package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link it.calolenoci.entity.AmmortamentoCespite} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmmortamentoCespiteDto implements Serializable {
    private String id;
    private String idAmmortamento;
    private String dataAmm;
    private String descrizione;
    private Double percAmm;
    private Double quota;
    private Double quotaRivalutazione;
    private Double fondo;
    private Double fondoRivalutazione;
    private Double fondoTot;
    private Double residuo;
    private Integer anno;
    private Double superPercentuale;
    private Double superQuota;
}