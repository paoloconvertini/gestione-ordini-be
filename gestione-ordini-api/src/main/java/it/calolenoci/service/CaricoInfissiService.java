package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.CaricoInfissiDto;
import it.calolenoci.dto.RipartizioneCaricoInfissiDto;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.MagazzinoMapper;
import it.calolenoci.mapper.SaldiMagazzinoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import it.calolenoci.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class CaricoInfissiService {
    @Inject EntityManager em;
    @Inject MagazzinoMapper magazzinoMapper;
    @Inject SaldiMagazzinoMapper saldiMapper;

    public RipartizioneCaricoInfissiDto anteprima(CaricoInfissiDto dto) {
        return ripartizione(dto, articoli(dto));
    }

    private List<OrdineDettaglio> articoli(CaricoInfissiDto dto) {
        if (dto == null || dto.getArticoli() == null || dto.getArticoli().isEmpty())
            throw new BusinessException("Selezionare gli articoli da caricare");
        Set<Integer> ids = new HashSet<>();
        List<OrdineDettaglio> result = new ArrayList<>();
        for (OrdineDettaglio richiesta : dto.getArticoli()) {
            if (richiesta == null || richiesta.getProgrGenerale() == null || !ids.add(richiesta.getProgrGenerale()))
                throw new BusinessException("Selezione articoli non valida o duplicata");
            OrdineDettaglio r = OrdineDettaglio.find("progrGenerale", richiesta.getProgrGenerale()).firstResult();
            if (r == null || Set.of("C", "AC").contains(StringUtils.defaultString(r.getTipoRigo()).trim())
                    || StringUtils.isBlank(r.getFArticolo()) || OrdineDettaglio.CODICI_DA_CODIFICARE.contains(r.getFArticolo()))
                throw new BusinessException("Selezionare soltanto righe articolo codificate");
            if (r.getQuantita() == null || !Double.isFinite(r.getQuantita()) || r.getQuantita() <= 0
                    || richiesta.getQuantita() == null || !Objects.equals(r.getQuantita(), richiesta.getQuantita()))
                throw new BusinessException("Quantità non valida o modificata: aggiornare il dettaglio ordine");
            if (!result.isEmpty() && (!Objects.equals(r.getAnno(), result.get(0).getAnno())
                    || !Objects.equals(r.getSerie(), result.get(0).getSerie())
                    || !Objects.equals(r.getProgressivo(), result.get(0).getProgressivo())))
                throw new BusinessException("Selezionare articoli dello stesso ordine cliente");
            result.add(r);
        }
        result.sort(Comparator.comparing(OrdineDettaglio::getRigo).thenComparing(OrdineDettaglio::getProgrGenerale));
        return result;
    }

    private RipartizioneCaricoInfissiDto ripartizione(CaricoInfissiDto dto, List<OrdineDettaglio> articoli) {
        BigDecimal totale = dto.getTotaleImponibile();
        if (totale == null || totale.signum() <= 0) throw new BusinessException("Inserire un imponibile maggiore di zero");
        try { totale = totale.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new BusinessException("L'imponibile deve avere al massimo due decimali"); }
        List<BigDecimal> quantita = articoli.stream().map(r -> BigDecimal.valueOf(r.getQuantita())).toList();
        BigDecimal somma = quantita.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BigDecimal> importi = ripartisci(totale, quantita);
        List<RipartizioneCaricoInfissiDto.Riga> righe = new ArrayList<>();
        for (int i = 0; i < articoli.size(); i++) {
            OrdineDettaglio r = articoli.get(i);
            righe.add(new RipartizioneCaricoInfissiDto.Riga(r.getProgrGenerale(), r.getRigo(),
                    r.getFArticolo(), r.getFDescrArticolo(), quantita.get(i), importi.get(i)));
        }
        return new RipartizioneCaricoInfissiDto(totale, somma, totale.divide(somma, 8, RoundingMode.HALF_UP), righe);
    }

    // Calcolo in centesimi: le frazioni maggiori ricevono i centesimi residui.
    static List<BigDecimal> ripartisci(BigDecimal totale, List<BigDecimal> quantita) {
        BigDecimal somma = quantita.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal centesimi = totale.movePointRight(2);
        List<BigDecimal> quote = new ArrayList<>();
        List<BigDecimal> resti = new ArrayList<>();
        BigDecimal assegnati = BigDecimal.ZERO;
        for (BigDecimal q : quantita) {
            BigDecimal numeratore = centesimi.multiply(q);
            BigDecimal quota = numeratore.divide(somma, 0, RoundingMode.DOWN);
            quote.add(quota); resti.add(numeratore.subtract(quota.multiply(somma)));
            assegnati = assegnati.add(quota);
        }
        List<Integer> ordine = new ArrayList<>();
        for (int i = 0; i < quantita.size(); i++) ordine.add(i);
        ordine.sort((a, b) -> resti.get(b).compareTo(resti.get(a)));
        int residui = centesimi.subtract(assegnati).intValueExact();
        for (int i = 0; i < residui; i++) {
            int indice = ordine.get(i); quote.set(indice, quote.get(indice).add(BigDecimal.ONE));
        }
        return quote.stream().map(q -> q.movePointLeft(2).setScale(2)).toList();
    }

    @Transactional
    public String salva(CaricoInfissiDto dto, String user) {
        List<OrdineDettaglio> articoli = articoli(dto);
        RipartizioneCaricoInfissiDto quote = ripartizione(dto, articoli);
        if (StringUtils.isBlank(dto.getNumDoc()) || dto.getNumDoc().trim().length() > 15
                || dto.getDataOperazione() == null || dto.getDataDocumento() == null
                || StringUtils.isBlank(dto.getCausale()) || StringUtils.isBlank(dto.getVettore()))
            throw new BusinessException("Completare i dati del documento (numero massimo 15 caratteri)");
        List<FornitoreArticoloId> fornitori = new ArrayList<>();
        for (OrdineDettaglio articolo : articoli) {
            List<FornitoreArticolo> candidati = FornitoreArticolo.find("fornitoreArticoloId.articolo", articolo.getFArticolo()).list();
            var predefiniti = candidati.stream().filter(f -> "S".equals(f.getFDefault())).toList();
            if (candidati.size() != 1 && predefiniti.size() != 1)
                throw new BusinessException("Fornitore mancante o ambiguo per " + articolo.getFArticolo());
            FornitoreArticoloId fornitore = (candidati.size() == 1 ? candidati.get(0) : predefiniti.get(0)).getFornitoreArticoloId();
            if (!fornitori.isEmpty() && (!Objects.equals(fornitore.getGruppo(), fornitori.get(0).getGruppo())
                    || !Objects.equals(StringUtils.leftPad(fornitore.getConto(), 6, '0'),
                        StringUtils.leftPad(fornitori.get(0).getConto(), 6, '0'))))
                throw new BusinessException("Selezionare articoli dello stesso fornitore per il DDT");
            fornitori.add(fornitore);
        }
        int anno = Year.now().getValue();
        // Mantiene i progressivi ERP, serializzando l'assegnazione durante il carico.
        int progressivo = ((Number) em.createNativeQuery("SELECT ISNULL(MAX(PROGRESSIVOMAG),0)+1 FROM MAGAZZINO WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ANNO=:anno AND SERIEMAGAZZINO='CF'").setParameter("anno", anno).getSingleResult()).intValue();
        int generale = ((Number) em.createNativeQuery("SELECT ISNULL(MAX(PROGRGENERALE),0) FROM MAGAZZINO WITH (UPDLOCK,HOLDLOCK)")
                .getSingleResult()).intValue();
        for (int i = 0; i < articoli.size(); i++) {
            OrdineDettaglio r = articoli.get(i);
            FornitoreArticoloId fornitore = fornitori.get(i);
            Magazzino m = magazzinoMapper.buildMagazzino(new MagazzinoId(anno, "CF", progressivo, " ", i + 1), ++generale,
                    r, fornitore.getGruppo(), StringUtils.leftPad(fornitore.getConto(), 6, '0'), dto.getNumDoc().trim(),
                    dto.getCausale(), dto.getVettore(), dto.getDataOperazione(), user, dto.getDataDocumento());
            BigDecimal importo = quote.getRighe().get(i).getImponibile();
            double unitario = importo.divide(BigDecimal.valueOf(r.getQuantita()), 8, RoundingMode.HALF_UP).doubleValue();
            m.setValore(importo.doubleValue()); m.setValoreUnitario(unitario); m.setPrezzo(unitario);
            // Il totale è già netto: non si riapplicano gli sconti dell'ordine cliente.
            m.setScontoarticolo(0D); m.setScontoc1(0D); m.setScontoc2(0D); m.setScontop(0D);
            m.persist();
            SaldiMagazzino saldo = SaldiMagazzino.find("marticolo=:art and mmagazzino='B'",
                    Parameters.with("art", r.getFArticolo())).firstResult();
            if (saldo == null) { saldo = saldiMapper.buildSaldiMagazzino(r, user); saldo.persist(); }
            else {
                double carichi = BigDecimal.valueOf(saldo.getQcarichi() == null ? 0 : saldo.getQcarichi())
                        .add(BigDecimal.valueOf(r.getQuantita())).setScale(2, RoundingMode.HALF_UP).doubleValue();
                saldo.setQcarichi(carichi); saldo.setQgiacenza(carichi - (saldo.getQscarichi() == null ? 0 : saldo.getQscarichi()));
            }
        }
        return "Creato carico infissi n. " + anno + "/CF/" + progressivo;
    }
}
