package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AttivitaMontaggioDettDto implements Serializable {

    private Long id;

    private Long idTipoAttivita;

    private String descrizioneTipoAttivita;

    private BigDecimal quantita;

    private Boolean completato;

    private String note;
}
