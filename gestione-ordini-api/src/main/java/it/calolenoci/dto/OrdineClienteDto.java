package it.calolenoci.dto;

import it.calolenoci.entity.OrdineDettaglioId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class OrdineClienteDto implements Serializable {
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
    private  Character geFlagRiservato;
    private  Character geFlagNonDisponibile;
    private  Character geFlagOrdinato;
    private  String geTono;
}
