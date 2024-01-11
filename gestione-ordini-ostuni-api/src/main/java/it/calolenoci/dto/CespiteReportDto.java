package it.calolenoci.dto;

import it.calolenoci.entity.AmmortamentoCespite;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CespiteReportDto implements Serializable {

    private String id;

    private String codice;

    private Integer progressivo1;

    private Integer progressivo2;

    private String cespite;

    private String dataAcq;

    private String numDocAcq;

    private String fornitore;

    private Double importo;

    private Double importoRivalutazione;

    private Double ammortamento;

    private Integer anno;

    private String superAmmDesc;

    private Double importoVendita;

    private String dataVend;

    private String protocollo;

    private List<AmmortamentoCespiteDto> ammortamentoCespiteList;
}
