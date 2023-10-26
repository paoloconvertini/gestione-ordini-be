package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class OrdineDettaglioDto implements Serializable {
    private  Integer anno;
    private  Integer progressivo;

    private  Integer progrGenerale;
    private  String tipoRigo;
    private  Integer rigo;
    private  String serie;
    private  String fArticolo;
    private  String codArtFornitore;
    private  String fDescrArticolo;
    private  Double quantita;
    private  Double prezzo;

    private  Double prezzoScontato;
    private  String fUnitaMisura;

    private Double scontoArticolo;

    private Double scontoC1;

    private Double scontoC2;

    private Double scontoP;

    private String fCodiceIva;

    private Integer fColli;
    private  Boolean flagRiservato;
    private  Boolean flagNonDisponibile;
    private  Boolean flagOrdinato;
    private  Boolean flagConsegnato;
    private  String tono;

    private Integer annoOAF;

    private String serieOAF;

    private Integer progressivoOAF;

    private String numeroBolla;

    private Date dataBolla;

    private Date dataOrdineOAF;

    private String articolo;

    private Double qtaDaConsegnare;

    private Double qtaConsegnatoSenzaBolla;

    private Boolean flBolla;

    private String intestazione;

    private String note;

    private Double qtaProntoConsegna;

    private Double qtaRiservata;

    private Boolean flProntoConsegna;

    private Double qtaBolla;

    private String noteOrdCli;


    private Date dataFattura;

    private String numeroFattura;

    private Date dataCarico;

    private String numDoc;

    private Date dataDoc;

    private Integer annoMag;

    private String serieMag;

    private Integer progressivoMag;

    //private String goFArticolo;

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, Double prezzo, String fUnitaMisura,
                              Boolean flagRiservato, Boolean flagNonDisponibile, Boolean flagOrdinato, Boolean flagConsegnato,
                              String tono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.flagRiservato = flagRiservato;
        this.flagNonDisponibile = flagNonDisponibile;
        this.flagOrdinato = flagOrdinato;
        this.flagConsegnato = flagConsegnato;
        this.tono = tono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, Double prezzo, String fUnitaMisura,
                              Boolean flagRiservato, Boolean flagNonDisponibile, Boolean flagOrdinato, Boolean flagConsegnato,
                              String tono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla, String intestazione) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.flagRiservato = flagRiservato;
        this.flagNonDisponibile = flagNonDisponibile;
        this.flagOrdinato = flagOrdinato;
        this.flagConsegnato = flagConsegnato;
        this.tono = tono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
        this.intestazione = intestazione;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, Double prezzo, String fUnitaMisura,
                              Boolean flagRiservato, Boolean flagNonDisponibile, Boolean flagOrdinato, Boolean flagConsegnato,
                              String tono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla, String intestazione,
                              String note, Double qtaRiservata, Boolean flProntoConsegna, Double qtaProntoConsegna) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.flagRiservato = flagRiservato;
        this.flagNonDisponibile = flagNonDisponibile;
        this.flagOrdinato = flagOrdinato;
        this.flagConsegnato = flagConsegnato;
        this.tono = tono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
        this.intestazione = intestazione;
        this.note = note;
        this.qtaRiservata = qtaRiservata;
        this.flProntoConsegna = flProntoConsegna;
        this.qtaProntoConsegna = qtaProntoConsegna;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo,
                              String serie, String fArticolo, String codArtFornitore, String fDescrArticolo,
                              Double quantita, Double prezzo, Double prezzoScontato, String fUnitaMisura,
                              Boolean flagRiservato, Boolean flagNonDisponibile, Boolean flagOrdinato, Boolean flagConsegnato,
                              //String goFArticolo,
                              String tono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla,
                              String note, Double qtaRiservata, Boolean flProntoConsegna, Double qtaProntoConsegna, String noteOrdCli,
                              Date dataDoc, String numDoc, Date dataCarico, Integer annoMag, String serieMag, Integer progressivoMag) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.prezzoScontato = prezzoScontato;
        this.fUnitaMisura = fUnitaMisura;
        this.flagRiservato = flagRiservato;
        this.flagNonDisponibile = flagNonDisponibile;
        this.flagOrdinato = flagOrdinato;
        this.flagConsegnato = flagConsegnato;
       // this.goFArticolo = goFArticolo;
        this.tono = tono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
        this.note = note;
        this.qtaRiservata = qtaRiservata;
        this.flProntoConsegna = flProntoConsegna;
        this.qtaProntoConsegna = qtaProntoConsegna;
        this.noteOrdCli = noteOrdCli;
        this.dataDoc = dataDoc;
        this.numDoc = numDoc;
        this.dataCarico = dataCarico;
        this.annoMag = annoMag;
        this.serieMag = serieMag;
        this.progressivoMag = progressivoMag;
    }

    public OrdineDettaglioDto (Integer anno, String serie, Integer progressivo, Integer rigo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
    }

    public OrdineDettaglioDto(Integer anno,String serie, Integer progressivo, Integer progrGenerale, Integer rigo,
                               Double quantita) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.rigo = rigo;
        this.quantita = quantita;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo,
                              String serie, String fArticolo, String codArtFornitore, String fDescrArticolo,
                              Double quantita, Double prezzo, String fUnitaMisura,
                              Integer fColli, Double scontoArticolo, Double scontoC1, Double scontoC2, Double scontoP, String fCodiceIva) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.fColli = fColli;
        this.scontoArticolo = scontoArticolo;
        this.scontoC1 = scontoC1;
        this.scontoC2 = scontoC2;
        this.scontoP = scontoP;
        this.fCodiceIva = fCodiceIva;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, String fUnitaMisura,
                              Boolean flagNonDisponibile, Boolean flagOrdinato, Boolean flagRiservato,
                              Double qtaDaConsegnare, String note, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF,Integer progrGenerale) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.fUnitaMisura = fUnitaMisura;
        this.flagNonDisponibile = flagNonDisponibile;
        this.flagOrdinato = flagOrdinato;
        this.flagRiservato = flagRiservato;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.note = note;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.progrGenerale = progrGenerale;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double qtaDaConsegnare, String fUnitaMisura,
                              String note, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF,Integer progrGenerale, Double prezzo) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.fUnitaMisura = fUnitaMisura;
        this.note = note;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.progrGenerale = progrGenerale;
        this.prezzo = prezzo;
    }
}
