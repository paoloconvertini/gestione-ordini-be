package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
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
    private  Boolean geFlagRiservato;
    private  Boolean geFlagNonDisponibile;
    private  Boolean geFlagOrdinato;
    private  Boolean geFlagConsegnato;
    private  String geTono;

    private Integer annoOAF;

    private String serieOAF;

    private Integer progressivoOAF;
    private String numeroBolla;
    private Date dataBolla;

    private Date dataOrdineOAF;

    public OrdineDettaglioDto(Integer anno, Integer progressivo, String tipoRigo, Integer rigo, String serie, String fArticolo, String codArtFornitore, String fDescrArticolo, Float quantita, Float prezzo, String fUnitaMisura, Float scontoArticolo, Float scontoC1, Float scontoC2, Float scontoP, String fCodiceIva, Integer fColli, Boolean geFlagRiservato, Boolean geFlagNonDisponibile, Boolean geFlagOrdinato, Boolean geFlagConsegnato, String geTono) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.scontoArticolo = scontoArticolo;
        this.scontoC1 = scontoC1;
        this.scontoC2 = scontoC2;
        this.scontoP = scontoP;
        this.fCodiceIva = fCodiceIva;
        this.fColli = fColli;
        this.geFlagRiservato = geFlagRiservato;
        this.geFlagNonDisponibile = geFlagNonDisponibile;
        this.geFlagOrdinato = geFlagOrdinato;
        this.geFlagConsegnato = geFlagConsegnato;
        this.geTono = geTono;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, String tipoRigo, Integer rigo, String serie, String fArticolo, String codArtFornitore, String fDescrArticolo, Float quantita, Float prezzo, String fUnitaMisura, Float scontoArticolo, Float scontoC1, Float scontoC2, Float scontoP, String fCodiceIva, Integer fColli, Boolean geFlagRiservato, Boolean geFlagNonDisponibile, Boolean geFlagOrdinato, Boolean geFlagConsegnato, String geTono, Integer annoOAF, String serieOAF, Integer progressivoOAF, Date dataOrdineOAF) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.scontoArticolo = scontoArticolo;
        this.scontoC1 = scontoC1;
        this.scontoC2 = scontoC2;
        this.scontoP = scontoP;
        this.fCodiceIva = fCodiceIva;
        this.fColli = fColli;
        this.geFlagRiservato = geFlagRiservato;
        this.geFlagNonDisponibile = geFlagNonDisponibile;
        this.geFlagOrdinato = geFlagOrdinato;
        this.geFlagConsegnato = geFlagConsegnato;
        this.geTono = geTono;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, String tipoRigo, Integer rigo, String serie, String fArticolo, String codArtFornitore, String fDescrArticolo, Float quantita, Float prezzo, String fUnitaMisura, Float scontoArticolo, Float scontoC1, Float scontoC2, Float scontoP, String fCodiceIva, Integer fColli, Boolean geFlagRiservato, Boolean geFlagNonDisponibile, Boolean geFlagOrdinato, Boolean geFlagConsegnato, String geTono, String numeroBolla, Date dataBolla) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.scontoArticolo = scontoArticolo;
        this.scontoC1 = scontoC1;
        this.scontoC2 = scontoC2;
        this.scontoP = scontoP;
        this.fCodiceIva = fCodiceIva;
        this.fColli = fColli;
        this.geFlagRiservato = geFlagRiservato;
        this.geFlagNonDisponibile = geFlagNonDisponibile;
        this.geFlagOrdinato = geFlagOrdinato;
        this.geFlagConsegnato = geFlagConsegnato;
        this.geTono = geTono;
        this.numeroBolla = numeroBolla;
        this.dataBolla = dataBolla;
    }
}
