package it.calolenoci.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RegistroCespiteReportDto implements Serializable {

    private String totInizioEsercizioTipoCespite;
    private Double totInizioEsercizioValoreAggiornato = 0D;
    private Double totInizioEsercizioAmmortamentoOrdinario = 0D;
    private Double totInizioEsercizioTotaleAmmortamento = 0D;
    private Double totInizioEsercizioFondoAmmortamenti = 0D;
    private Double totInizioEsercizioResiduo = 0D;
    private Double totAcquistiValoreAggiornato = 0D;
    private Double totAcquistiAmmortamentoOrdinario = 0D;
    private Double totAcquistiTotaleAmmortamento = 0D;
    private Double totAcquistiFondoAmmortamenti = 0D;
    private Double totAcquistiResiduo = 0D;
    private Double totVenditeValoreAggiornato = 0D;
    private Double totVenditeAmmortamentoOrdinario = 0D;
    private Double totVenditeTotaleAmmortamento = 0D;
    private Double totVenditeFondoAmmortamenti = 0D;
    private Double totVenditeResiduo = 0D;
    private String totAmmortamentiDeducibiliTipoCespite;
    private Double totAmmortamentiDeducibiliValoreAggiornato = 0D;
    private Double totAmmortamentiDeducibiliAmmortamentoOrdinario = 0D;
    private Double totAmmortamentiDeducibiliTotaleAmmortamento = 0D;
    private Double totAmmortamentiDeducibiliFondoAmmortamenti = 0D;
    private Double totAmmortamentiDeducibiliResiduo = 0D;
    private Double totFineEsercizioValoreAggiornato = 0D;
    private Double totFineEsercizioAmmortamentoOrdinario = 0D;
    private Double totFineEsercizioTotaleAmmortamento = 0D;
    private Double totFineEsercizioFondoAmmortamenti = 0D;
    private Double totFineEsercizioResiduo = 0D;
    private String totSuperAmm1Desc;
    private Double totSuperAmm1Totale;
    private String totSuperAmm2Desc;
    private Double totSuperAmm2Totale;
    private String totSuperAmm3Desc;
    private Double totSuperAmm3Totale;
    private Double totSuperAmmortamenti = 0D;
    private String totPlusMinusDesc;
    private Double totPlusMinusTotale = 0D;
    private Boolean showTotali = Boolean.FALSE;
    private Boolean showPlus = Boolean.FALSE;

    List<CategoriaCespiteReportDto> categoriaCespiteReportDtoList;

}
