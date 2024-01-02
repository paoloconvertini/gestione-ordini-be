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
    private Double catInizioEsercizioAmmortamentoAnticipato = 0D;
    private Double catInizioEsercizioTotaleAmmortamento = 0D;
    private Double catInizioEsercizioNonAmmortabile = 0D;
    private Double catInizioEsercizioFondoAmmortamenti = 0D;
    private Double catInizioEsercizioFondoAmmortamentiRiv = 0D;
    private Double catInizioEsercizioFondoAmmortamentiTot = 0D;
    private Double catInizioEsercizioResiduo = 0D;
    private String catAcquistiTipoCespite;
    private Double catAcquistiValoreAggiornato = 0D;
    private Double catAcquistiAmmortamentoOrdinario = 0D;
    private Double catAcquistiAmmortamentoAnticipato = 0D;
    private Double catAcquistiTotaleAmmortamento = 0D;
    private Double catAcquistiNonAmmortabile = 0D;
    private Double catAcquistiFondoAmmortamenti = 0D;
    private Double catAcquistiFondoAmmortamentiRiv = 0D;
    private Double catAcquistiFondoAmmortamentiTot = 0D;
    private Double catAcquistiResiduo = 0D;
    private String catVenditeTipoCespite;
    private Double catVenditeValoreAggiornato = 0D;
    private Double catVenditeAmmortamentoOrdinario = 0D;
    private Double catVenditeAmmortamentoAnticipato = 0D;
    private Double catVenditeTotaleAmmortamento = 0D;
    private Double catVenditeNonAmmortabile = 0D;
    private Double catVenditeFondoAmmortamenti = 0D;
    private Double catVenditeFondoAmmortamentiRiv = 0D;
    private Double catVenditeFondoAmmortamentiTot = 0D;
    private Double catVenditeResiduo = 0D;
    private String catAmmortamentiDeducibiliTipoCespite;
    private Double catAmmortamentiDeducibiliValoreAggiornato = 0D;
    private Double catAmmortamentiDeducibiliAmmortamentoOrdinario = 0D;
    private Double catAmmortamentiDeducibiliAmmortamentoAnticipato = 0D;
    private Double catAmmortamentiDeducibiliTotaleAmmortamento = 0D;
    private Double catAmmortamentiDeducibiliNonAmmortabile = 0D;
    private Double catAmmortamentiDeducibiliFondoAmmortamenti = 0D;
    private Double catAmmortamentiDeducibiliFondoAmmortamentiRiv = 0D;
    private Double catAmmortamentiDeducibiliFondoAmmortamentiTot = 0D;
    private Double catAmmortamentiDeducibiliResiduo = 0D;
    private String catAmmortamentiNonDeducibiliTipoCespite;
    private Double catAmmortamentiNonDeducibiliValoreAggiornato = 0D;
    private Double catAmmortamentiNonDeducibiliAmmortamentoOrdinario = 0D;
    private Double catAmmortamentiNonDeducibiliAmmortamentoAnticipato = 0D;
    private Double catAmmortamentiNonDeducibiliTotaleAmmortamento = 0D;
    private Double catAmmortamentiNonDeducibiliNonAmmortabile = 0D;
    private Double catAmmortamentiNonDeducibiliFondoAmmortamenti = 0D;
    private Double catAmmortamentiNonDeducibiliFondoAmmortamentiRiv = 0D;
    private Double catAmmortamentiNonDeducibiliFondoAmmortamentiTot = 0D;
    private Double catAmmortamentiNonDeducibiliResiduo = 0D;
    private String catFineEsercizioTipoCespite;
    private Double catFineEsercizioValoreAggiornato = 0D;
    private Double catFineEsercizioAmmortamentoOrdinario = 0D;
    private Double catFineEsercizioAmmortamentoAnticipato = 0D;
    private Double catFineEsercizioTotaleAmmortamento = 0D;
    private Double catFineEsercizioNonAmmortabile = 0D;
    private Double catFineEsercizioFondoAmmortamenti = 0D;
    private Double catFineEsercizioFondoAmmortamentiRiv = 0D;
    private Double catFineEsercizioFondoAmmortamentiTot = 0D;
    private Double catFineEsercizioResiduo = 0D;
    private String catSuperAmm1Desc;
    private Double catSuperAmm1Totale;
    private String catSuperAmm2Desc;
    private Double catSuperAmm2Totale;
    private String catSuperAmm3Desc;
    private Double catSuperAmm3Totale;
    private String catSuperAmm4Desc;
    private Double catSuperAmm4Totale;
    private String catPlusMinusDesc;
    private Double catPlusMinusTotale;

    private List<CespiteReportDto> cespiteDtoList;
}
