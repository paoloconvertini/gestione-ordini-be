package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.FattureDettaglio;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link FattureDettaglio} entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class FatturaDto implements Serializable {
    private Integer progrOrdCli;
    private Double qta;

    private Double prezzo;
    private String numeroBolla;
    private Date dataBolla;

    public FatturaDto(Integer progrOrdCli, Double qta) {
        this.progrOrdCli = progrOrdCli;
        this.qta = qta;
    }

    public FatturaDto(String numeroBolla, Date dataBolla, Double qta) {
        this.numeroBolla = numeroBolla;
        this.dataBolla = dataBolla;
        this.qta = qta;
    }
}