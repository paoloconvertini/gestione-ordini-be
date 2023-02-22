package it.calolenoci.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class OrdineDettaglioDto implements Serializable {
    private  Integer anno;
    private  Integer progressivo;
    private  String tipoRigo;
    private  Integer rigo;
    private  String serie;
    private  String fArticolo;
    private  String codArtFornitore;
    private  String fDescrArticolo;
    private  Float quantita;
    private  Float prezzo;
    private  String fUnitaMisura;
    private Float scontoArticolo;
    private Float scontoC1;
    private Float scontoC2;
    private Float scontoP;
    private String fCodiceIva;
    private Integer fColli;
    private  Character geFlagRiservato;
    private  Character geFlagNonDisponibile;
    private  Character geFlagOrdinato;
    private  String geTono;
}
