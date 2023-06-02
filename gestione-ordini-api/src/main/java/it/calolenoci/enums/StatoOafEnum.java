package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatoOafEnum {

    SOSPESO("SOSPESO"),
    DA_APPROVARE("DA_ORDINARE"),
    APPROVATO("INCOMPLETO");

    private final String descrizione;
}
