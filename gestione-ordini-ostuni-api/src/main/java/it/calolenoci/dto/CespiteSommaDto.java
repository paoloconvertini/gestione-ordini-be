package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteSommaDto {

    private FiscaleRiepilogoDto inizioEsercizio;
    private FiscaleRiepilogoDto acquisti;
    private FiscaleRiepilogoDto vendite;
    private FiscaleRiepilogoDto ammortamentiDeducibili;
    private FiscaleRiepilogoDto ammortamentiNonDeducibili;
    private FiscaleRiepilogoDto fineEsercizio;
    private SuperAmmDto superAmm1;
    private SuperAmmDto superAmm2;
    private SuperAmmDto superAmm3;
    private SuperAmmDto superAmm4;
    private SuperAmmDto plusMinus;
}
