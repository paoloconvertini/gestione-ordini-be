package it.calolenoci.dto;

import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.Magazzino;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaricoDto implements Serializable {

    private GoOrdine goOrdine;

    private GoOrdineDettaglio goOrdineDettaglio;

    private Magazzino magazzino;

}
