package it.calolenoci.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CategoriaCespiteReportDto implements Serializable {


    private String tipoCespite;

    private String categoria;

    private Double perc;

    private String catInizioEsercizioTipoCespite;
    private Double catInizioEsercizioValoreAggiornato = 0D;
    private Double catInizioEsercizioAmmortamentoOrdinario = 0D;
    private Double catInizioEsercizioTotaleAmmortamento = 0D;
    private Double catInizioEsercizioFondoAmmortamenti = 0D;
    private Double catInizioEsercizioResiduo = 0D;
    private Double catAcquistiValoreAggiornato = 0D;
    private Double catAcquistiAmmortamentoOrdinario = 0D;
    private Double catAcquistiTotaleAmmortamento = 0D;
    private Double catAcquistiFondoAmmortamenti = 0D;
    private Double catAcquistiResiduo = 0D;
    private Double catVenditeValoreAggiornato = 0D;
    private Double catVenditeAmmortamentoOrdinario = 0D;
    private Double catVenditeTotaleAmmortamento = 0D;
    private Double catVenditeFondoAmmortamenti = 0D;
    private Double catVenditeResiduo = 0D;
    private Double catAmmortamentiDeducibiliValoreAggiornato = 0D;
    private Double catAmmortamentiDeducibiliAmmortamentoOrdinario = 0D;
    private Double catAmmortamentiDeducibiliTotaleAmmortamento = 0D;
    private Double catAmmortamentiDeducibiliFondoAmmortamenti = 0D;
    private Double catAmmortamentiDeducibiliResiduo = 0D;
    private Double catAmmortamentiNonDeducibiliValoreAggiornato = 0D;
    private Double catAmmortamentiNonDeducibiliAmmortamentoOrdinario = 0D;
    private Double catAmmortamentiNonDeducibiliTotaleAmmortamento = 0D;
    private Double catAmmortamentiNonDeducibiliFondoAmmortamenti = 0D;
    private Double catAmmortamentiNonDeducibiliResiduo = 0D;
    private Double catFineEsercizioValoreAggiornato = 0D;
    private Double catFineEsercizioAmmortamentoOrdinario = 0D;
    private Double catFineEsercizioTotaleAmmortamento = 0D;
    private Double catFineEsercizioFondoAmmortamenti = 0D;
    private Double catFineEsercizioResiduo = 0D;
    private String catSuperAmm1Desc;
    private Double catSuperAmm1Totale = 0D;
    private String catSuperAmm2Desc;
    private Double catSuperAmm2Totale = 0D;
    private String catSuperAmm3Desc;
    private Double catSuperAmm3Totale = 0D;
    private Double catSuperAmmortamenti;
    private String catPlusMinusDesc;
    private Double catPlusMinusTotale = 0D;

    private List<CespiteReportDto> cespiteDtoList;


    private Boolean catShowTotali = Boolean.FALSE;
    private Boolean catShowPlus = Boolean.FALSE;
}
