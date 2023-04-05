package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatoOafEnum {

    DA_PROCESSARE("DA_PROCESSARE"),
    DA_APPROVARE("DA_ORDINARE"),
    APPROVATO("INCOMPLETO");

    private final String descrizione;
}
