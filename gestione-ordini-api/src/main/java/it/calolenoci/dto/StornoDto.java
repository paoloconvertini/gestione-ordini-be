package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StornoDto implements Serializable {
    private String fCodiceIva; // descrizione articolo (con numero fattura acconto dentro)
    private Double importo;  // importo dello storno

}