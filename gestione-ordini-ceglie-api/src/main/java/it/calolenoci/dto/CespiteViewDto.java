package it.calolenoci.dto;

import it.calolenoci.entity.AmmortamentoCespite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteViewDto implements Serializable {

    private String codice;

    private Integer progressivo1;

    private Integer progressivo2;

    private String cespite;

    private LocalDate dataAcq;

    private String numDocAcq;

    private String fornitore;

    private Double importo;

    private Double ammortamento;

    private Integer anno;

    private String superAmmDesc;

    private Double importoVendita;

    private LocalDate dataVend;

    private Integer protocollo;

    private String giornale;

    private Integer annoProtocollo;

    private List<AmmortamentoCespite> ammortamentoCespiteList;

}
