package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.OrdineFornitore} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdineFornitoreDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer gruppo;
    private String conto;
    private Date dataOrdine;
    private Date dataConfOrdine;
    private String numConfOrdine;
    private String provvisorio;

    private String intestazione;

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, Date dataOrdine, String intestazione, Date dataConfOrdine, String numConfOrdine, String provvisorio) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.intestazione = intestazione;
        this.dataOrdine = dataOrdine;
        this.dataConfOrdine = dataConfOrdine;
        this.numConfOrdine = numConfOrdine;
        this.provvisorio = provvisorio;
    }
}