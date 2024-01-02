package it.calolenoci.dto;

import lombok.Builder;
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
    private Double totInizioEsercizioAmmortamentoAnticipato = 0D;
    private Double totInizioEsercizioTotaleAmmortamento = 0D;
    private Double totInizioEsercizioNonAmmortabile = 0D;
    private Double totInizioEsercizioFondoAmmortamenti = 0D;
    private Double totInizioEsercizioFondoAmmortamentiRiv = 0D;
    private Double totInizioEsercizioFondoAmmortamentiTot = 0D;
    private Double totInizioEsercizioResiduo = 0D;
    private String totAcquistiTipoCespite;
    private Double totAcquistiValoreAggiornato = 0D;
    private Double totAcquistiAmmortamentoOrdinario = 0D;
    private Double totAcquistiAmmortamentoAnticipato = 0D;
    private Double totAcquistiTotaleAmmortamento = 0D;
    private Double totAcquistiNonAmmortabile = 0D;
    private Double totAcquistiFondoAmmortamenti = 0D;
    private Double totAcquistiFondoAmmortamentiRiv = 0D;
    private Double totAcquistiFondoAmmortamentiTot = 0D;
    private Double totAcquistiResiduo = 0D;
    private String totVenditeTipoCespite;
    private Double totVenditeValoreAggiornato = 0D;
    private Double totVenditeAmmortamentoOrdinario = 0D;
    private Double totVenditeAmmortamentoAnticipato = 0D;
    private Double totVenditeTotaleAmmortamento = 0D;
    private Double totVenditeNonAmmortabile = 0D;
    private Double totVenditeFondoAmmortamenti = 0D;
    private Double totVenditeFondoAmmortamentiRiv = 0D;
    private Double totVenditeFondoAmmortamentiTot = 0D;
    private Double totVenditeResiduo = 0D;
    private String totAmmortamentiDeducibiliTipoCespite;
    private Double totAmmortamentiDeducibiliValoreAggiornato = 0D;
    private Double totAmmortamentiDeducibiliAmmortamentoOrdinario = 0D;
    private Double totAmmortamentiDeducibiliAmmortamentoAnticipato = 0D;
    private Double totAmmortamentiDeducibiliTotaleAmmortamento = 0D;
    private Double totAmmortamentiDeducibiliNonAmmortabile = 0D;
    private Double totAmmortamentiDeducibiliFondoAmmortamenti = 0D;
    private Double totAmmortamentiDeducibiliFondoAmmortamentiRiv = 0D;
    private Double totAmmortamentiDeducibiliFondoAmmortamentiTot = 0D;
    private Double totAmmortamentiDeducibiliResiduo = 0D;
    private String totAmmortamentiNonDeducibiliTipoCespite;
    private Double totAmmortamentiNonDeducibiliValoreAggiornato = 0D;
    private Double totAmmortamentiNonDeducibiliAmmortamentoOrdinario = 0D;
    private Double totAmmortamentiNonDeducibiliAmmortamentoAnticipato = 0D;
    private Double totAmmortamentiNonDeducibiliTotaleAmmortamento = 0D;
    private Double totAmmortamentiNonDeducibiliNonAmmortabile = 0D;
    private Double totAmmortamentiNonDeducibiliFondoAmmortamenti = 0D;
    private Double totAmmortamentiNonDeducibiliFondoAmmortamentiRiv = 0D;
    private Double totAmmortamentiNonDeducibiliFondoAmmortamentiTot = 0D;
    private Double totAmmortamentiNonDeducibiliResiduo = 0D;
    private String totFineEsercizioTipoCespite;
    private Double totFineEsercizioValoreAggiornato = 0D;
    private Double totFineEsercizioAmmortamentoOrdinario = 0D;
    private Double totFineEsercizioAmmortamentoAnticipato = 0D;
    private Double totFineEsercizioTotaleAmmortamento = 0D;
    private Double totFineEsercizioNonAmmortabile = 0D;
    private Double totFineEsercizioFondoAmmortamenti = 0D;
    private Double totFineEsercizioFondoAmmortamentiRiv = 0D;
    private Double totFineEsercizioFondoAmmortamentiTot = 0D;
    private Double totFineEsercizioResiduo = 0D;
    private String totSuperAmm1Desc;
    private Double totSuperAmm1Totale;
    private String totSuperAmm2Desc;
    private Double totSuperAmm2Totale;
    private String totSuperAmm3Desc;
    private Double totSuperAmm3Totale;
    private String totSuperAmm4Desc;
    private Double totSuperAmm4Totale;
    private String totPlusMinusDesc;
    private Double totPlusMinusTotale;

    List<CategoriaCespiteReportDto> categoriaCespiteReportDtoList;

}