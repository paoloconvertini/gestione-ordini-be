package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteSommaDto {

    private FiscaleRiepilogo inizioEsercizio;
    private FiscaleRiepilogo acquisti;
    private FiscaleRiepilogo vendite;
    private FiscaleRiepilogo ammortamentiDeducibili;
    private FiscaleRiepilogo ammortamentiNonDeducibili;
    private FiscaleRiepilogo fineEsercizio;
}
