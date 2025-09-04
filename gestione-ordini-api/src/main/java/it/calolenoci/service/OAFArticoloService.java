package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.GoOrdineFornitoreMapper;
import it.calolenoci.mapper.OafArticoloMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Year;
import java.util.*;

@ApplicationScoped
public class OAFArticoloService {

    @Inject
    OafArticoloMapper mapper;

    @Inject
    GoOrdineFornitoreMapper goOrdineFornitoreMapper;


    public ResponseOAFDettaglioDTO findById(Integer anno, String serie, Integer progressivo) {
        ResponseOAFDettaglioDTO responseOAFDettaglioDTO = new ResponseOAFDettaglioDTO();
        Optional<OrdineFornitoreDto> optional = OrdineFornitore.find("SELECT f.anno, f.serie, f.progressivo, f.gruppo, f.conto, pc.intestazione " +
                "FROM OrdineFornitore f " +
                "JOIN PianoConti pc ON f.gruppo = pc.gruppoConto AND f.conto = pc.sottoConto  " +
                "WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
        .project(OrdineFornitoreDto.class).singleResultOptional();
        List<OrdineFornitoreDettaglioDto> list = OrdineFornitoreDettaglio.find("select o.anno, o.serie, o.progressivo, o.rigo, o.nota, o.oArticolo, " +
                " o.oDescrArticolo,  o.oQuantita, o.oPrezzo, o.oUnitaMisura, o.fScontoArticolo, o.scontoF1, o.scontoF2, o.fScontoP, o.tipoRigo, o.pid, o.campouser1 " +
                " FROM OrdineFornitoreDettaglio o " +
                //" LEFT JOIN OrdineDettaglio oc ON o.nota like CONCAT('Riferimento n. ', trim(str(oc.anno)), '/', oc.serie, '/', trim(str(oc.progressivo)), '-', trim(str(oc.rigo)))" +
                " WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo ORDER BY o.rigo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineFornitoreDettaglioDto.class).list();
        if(optional.isPresent()){
            responseOAFDettaglioDTO.setIntestazione(optional.get().getIntestazione());
            responseOAFDettaglioDTO.setSottoConto(optional.get().getConto());
        }
        responseOAFDettaglioDTO.setArticoli(list);
        return responseOAFDettaglioDTO;
    }

    @Transactional
    public void approva(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = '' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
        Optional<GoOrdineFornitore> opt = GoOrdineFornitore.findByIdOptional(new FornitoreId(anno, serie, progressivo));
        if(opt.isPresent()){
            opt.get().setFlInviato(Boolean.FALSE);
        } else {
            GoOrdineFornitore goOrdineFornitore = goOrdineFornitoreMapper.creaEntity(anno, serie, progressivo);
            goOrdineFornitore.persist();
        }

    }

    @Transactional
    public void richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = 'T' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
    }

    @Transactional
    public void save(List<OrdineFornitoreDettaglioDto> list) {
        list.forEach(o -> {
            OrdineFornitoreDettaglio entity = OrdineFornitoreDettaglio.findById(new FornitoreDettaglioId(o.getAnno(), o.getSerie(), o.getProgressivo(), o.getRigo()));
            mapper.viewToEntity(entity, o);
            entity.persist();
        });
    }

    @Transactional
    public boolean save(ArticoloDto dto, String user) {
        try {
            OrdineFornitoreDettaglio.update("rigo = (rigo+1) WHERE anno = :anno AND serie = :serie" +
                            " AND progressivo = :progressivo and rigo >=:rigo",
                    Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                            .and("progressivo", dto.getProgressivo()).and("rigo", dto.getRigo()));
            mapper.fromDtoToEntity(dto, user).persist();
            return true;
        } catch (Exception e) {
            Log.error("Errore nella creazione del rigo, " + e.getMessage());
            return false;
        }
    }

    public List<ArticoloDto> cercaArticoli(FiltroArticoli filtro){
        String query = "SELECT a.articolo, a.descrArticolo, a.descrArtSuppl, a.unitaMisura FROM Articolo a " +
                " WHERE 1=1 ";
        Map<String, Object> parameters = new HashMap<>();
        if(StringUtils.isNotBlank(filtro.getCodice())) {
            query += " AND a.articolo LIKE :codice ";
            parameters.put("codice", "%"+filtro.getCodice()+"%");
        }
        if(StringUtils.isNotBlank(filtro.getDescrizione())) {
            query += " AND a.descrArticolo LIKE :descrizione ";
            parameters.put("descrizione", "%"+filtro.getDescrizione()+"%");
        }
        if(StringUtils.isNotBlank(filtro.getDescrSuppl())) {
            query += " AND a.descrArtSuppl LIKE :descrSuppl ";
            parameters.put("descrSuppl", "%"+filtro.getDescrSuppl()+"%");
        }

        List<ArticoloDto> list = Articolo.find(query, parameters).project(ArticoloDto.class).list();
        list.forEach(e -> Magazzino.find("Select valoreUnitario FROM Magazzino " +
                                " WHERE mArticolo = :codArticolo AND valoreUnitario <> null " +
                                " and valoreUnitario <> '' " +
                                " and valoreUnitario <> ' ' " +
                                " ORDER BY dataMagazzino desc ",
                        Parameters.with("codArticolo", e.getArticolo()))
                .project(Double.class)
                .firstResultOptional()
                .ifPresent(e::setPrezzoBase));
        return list;
    }

    @Transactional
    public ResponseDto eliminaArticolo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        ResponseDto dto = new ResponseDto();
        OrdineFornitoreDettaglio articolo = OrdineFornitoreDettaglio.findById(new FornitoreDettaglioId(anno, serie, progressivo, rigo));
        if(articolo == null){
            Log.error("articolo fornitore non trovato al rigo = " + rigo);
            dto.setError(Boolean.TRUE);
            dto.setMsg("articolo fornitore non trovato al rigo = " + rigo);
            return dto;
        }
        Integer progrGeneraleCliente = articolo.getPid();
        if(progrGeneraleCliente != null && progrGeneraleCliente != 0) {
            Optional<OrdineDettaglio> optArticoloCliente = OrdineDettaglio.find("progrGenerale =:pid",
                    Parameters.with("pid", progrGeneraleCliente)).singleResultOptional();
            if(optArticoloCliente.isEmpty()) {
                Log.error("articolo cliente non trovato con progrGenerale = " + progrGeneraleCliente);
                dto.setError(Boolean.TRUE);
                dto.setMsg("articolo cliente non trovato con progrGenerale = " + progrGeneraleCliente);
                return dto;
            }
            OrdineDettaglio o = optArticoloCliente.get();
            GoOrdineDettaglio.update("flagOrdinato = 'F', flagNonDisponibile = 'T' " +
                            "WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo and rigo =:rigo",
                    Parameters.with("anno", o.getAnno()).and("serie", o.getSerie())
                            .and("progressivo", o.getProgressivo()).and("rigo", o.getRigo()));
            GoOrdine.update("status='DA_ORDINARE' WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo and status not in ('DA_ORDINARE','DA_PROCESSARE', 'ARCHIVIATO')",
                    Parameters.with("anno", o.getAnno()).and("serie", o.getSerie()).and("progressivo", o.getProgressivo()));

        }
        OrdineFornitoreDettaglio.deleteById(new FornitoreDettaglioId(anno, serie, progressivo, rigo));
        dto.setError(Boolean.FALSE);
        dto.setMsg("Articolo eliminato");
        return dto;
    }

    @Transactional
    public ResponseDto collegaOAF(Integer progrGeneraleOrdCli, ArticoloDto dto, String user) {
        ResponseDto result = new ResponseDto();

        try {
            // 1) Validazioni base
            if (progrGeneraleOrdCli == null) {
                result.setError(Boolean.TRUE);
                result.setMsg("Collega OAF: progrGenerale dell'ordine cliente mancante");
                return result;
            }
            if (dto == null) {
                result.setError(Boolean.TRUE);
                result.setMsg("Collega OAF: dto nullo");
                return result;
            }

            // 2) Recupero riga ordine cliente (quantità da legare)
            Optional<OrdineDettaglio> optCli = OrdineDettaglio
                    .find("progrGenerale = :p", Parameters.with("p", progrGeneraleOrdCli))
                    .singleResultOptional();

            if (optCli.isEmpty()) {
                Log.error("Collega OAF: ordine cliente non trovato con progrGenerale = " + progrGeneraleOrdCli);
                result.setError(Boolean.TRUE);
                result.setMsg("Ordine cliente non trovato (progrGenerale=" + progrGeneraleOrdCli + ")");
                return result;
            }
            OrdineDettaglio ordineDettaglio = optCli.get();
            Double qtaCliente = (ordineDettaglio.getQuantita() == null ? 0D : ordineDettaglio.getQuantita());

            // 3) Recupero riga OAF di partenza (anno/serie/progressivo/rigo presi dal dto)
            Optional<OrdineFornitoreDettaglio> optOaf = OrdineFornitoreDettaglio.find(
                            "anno = :a AND serie = :s AND progressivo = :p AND rigo = :r",
                            Parameters.with("a", dto.getAnno())
                                    .and("s", dto.getSerie())
                                    .and("p", dto.getProgressivo())
                                    .and("r", dto.getRigo()))
                    .singleResultOptional();

            if (optOaf.isEmpty()) {
                result.setError(Boolean.TRUE);
                result.setMsg("Riga OAF non trovata: " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo() + " rigo " + dto.getRigo());
                return result;
            }
            OrdineFornitoreDettaglio oafOrig = optOaf.get();
            Double qtaOAF = (oafOrig.getOQuantita() == null ? 0D : oafOrig.getOQuantita());

            // 4) Coerenza quantità
            if (qtaCliente <= 0) {
                result.setError(Boolean.TRUE);
                result.setMsg("Quantità ordine cliente nulla o negativa: " + qtaCliente);
                return result;
            }
            if (qtaCliente > qtaOAF) {
                result.setError(Boolean.TRUE);
                result.setMsg("Quantità cliente (" + qtaCliente + ") superiore a quantità OAF (" + qtaOAF + ")");
                return result;
            }

            // 5) Riduco la riga OAF originale (parte che resta “a magazzino”)
            double qtaResidua = qtaOAF - qtaCliente;
            oafOrig.setOQuantita(qtaResidua);
            oafOrig.setOQuantitaV(qtaResidua);
            // (eventuale ricalcolo valoreTotale se lo gestisci qui; lasciato com'è se lo calcoli altrove)
            oafOrig.persist(); // Panache: dirty checking, ma persisto per chiarezza

            // 6) Calcolo append in coda (nuovi rigo / nuovi progrGenerale)
            Integer maxRigo = OrdineFornitoreDettaglio
                    .find("SELECT CASE WHEN MAX(f.rigo) IS NULL THEN 0 ELSE MAX(f.rigo) END " +
                                    "FROM OrdineFornitoreDettaglio f " +
                                    "WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo",
                            Parameters.with("anno", dto.getAnno())
                                    .and("serie", dto.getSerie())
                                    .and("progressivo", dto.getProgressivo()))
                    .project(Integer.class)
                    .firstResult();

            Integer maxProgrGen = OrdineFornitoreDettaglio
                    .find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM OrdineFornitoreDettaglio")
                    .project(Integer.class)
                    .firstResult();

            int baseRigo = (maxRigo == null ? 0 : maxRigo);                 // base per avere +1 e +2
            int baseProgrGen = (maxProgrGen == null ? 0 : maxProgrGen);     // base per avere +1 e +2

            // 7) Intestazione cliente (per riga commento)
            String intestazione = "";
            Optional<String> optInt = Ordine.find("SELECT p.intestazione " +
                                    "FROM PianoConti p " +
                                    "JOIN Ordine o on o.contoCliente = p.sottoConto AND o.gruppoCliente = p.gruppoConto " +
                                    " AND o.anno = :a and o.serie = :s and o.progressivo = :p",
                            Parameters.with("a", ordineDettaglio.getAnno())
                                    .and("s", ordineDettaglio.getSerie())
                                    .and("p", ordineDettaglio.getProgressivo()))
                    .project(String.class)
                    .singleResultOptional();
            if (optInt.isPresent()) {
                intestazione = optInt.get();
            }

            // 8) Nuova riga ARTICOLO “cliente”, appesa in fondo
            //    uso il mapper.copyEntity(..): lui fa rigo = (paramRigo + 1) e progrGenerale = (progressivoFornDettaglio + 1)
            OrdineFornitoreDettaglio nuovaRigaCliente = mapper.copyEntity(
                    oafOrig,
                    progrGeneraleOrdCli,              // pid = progrGenerale dell'ordine cliente
                    baseRigo,                         // così diventa baseRigo+1 (append)
                    baseProgrGen,                     // così diventa baseProgrGen+1
                    ordineDettaglio                   // per quantità, campoUser5, nota, ecc.
            );
            // quantità della riga cliente = quantità ordine cliente
            nuovaRigaCliente.setOQuantita(qtaCliente);
            nuovaRigaCliente.setOQuantitaV(qtaCliente);
            if (nuovaRigaCliente.getOQuantita() != null && oafOrig.getOPrezzo() != null) {
                nuovaRigaCliente.setValoreTotale(nuovaRigaCliente.getOQuantita() * oafOrig.getOPrezzo());
            }
            nuovaRigaCliente.persist();

            // 9) Riga di COMMENTO immediatamente dopo (append in fondo)
            //    createRigoRiferimento fa rigo = (paramRigo + 1) e progrGenerale = (paramProgrGen + 1)
            OrdineFornitoreDettaglio rigoCommento = mapper.createRigoRiferimento(
                    dto.getSerie(),                    // serie OAF
                    dto.getProgressivo(),              // progressivo OAF
                    intestazione,                      // "Rif. <intestazione>"
                    baseRigo + 1,                      // -> (baseRigo+1)+1 = baseRigo+2 (subito dopo nuova riga cliente)
                    baseProgrGen + 1,                  // -> (baseProgrGen+1)+1
                    user
            );
            rigoCommento.persist();

            result.setError(Boolean.FALSE);
            result.setMsg("Collegato all'OAF " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo()
                    + ": riga cliente aggiunta (rigo " + nuovaRigaCliente.getRigo() + ") e commento (rigo " + rigoCommento.getRigo() + "). "
                    + "Residuo riga originale: " + qtaResidua);
            return result;

        } catch (Exception e) {
            Log.error("Collega OAF: ERROR! ", e);
            result.setError(Boolean.TRUE);
            result.setMsg("Collega OAF: ERROR! " + e.getMessage());
            return result;
        }
    }

}
