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
    public ResponseDto collegaOAF(Integer progrGenerale, ArticoloDto dto, String user) {
        ResponseDto result = new ResponseDto();
        try {
            Optional<OrdineDettaglio> opt = OrdineDettaglio.find("progrGenerale = :p", Parameters.with("p", progrGenerale)).singleResultOptional();
            if(opt.isEmpty()) {
                Log.error("Collega OAF: articolo non torvato con progrGenerale= " + progrGenerale);
                result.setError(Boolean.FALSE);
                result.setMsg("Collega OAF: articolo non torvato con progrGenerale= " + progrGenerale);
                return result;
            }

            Integer progressivoFornDettaglio = OrdineFornitoreDettaglio.
                    find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END " +
                            "FROM OrdineFornitoreDettaglio").project(Integer.class).firstResult();

            OrdineDettaglio ordineDettaglio = opt.get();
            String intestazione = "";
            Optional<String> optional = Ordine.find("SELECT p.intestazione " +
                                    "FROM PianoConti p " +
                                    "JOIN Ordine o on o.contoCliente = p.sottoConto AND o.gruppoCliente = p.gruppoConto" +
                                    " AND o.anno = :a and o.serie = :s and o.progressivo = :p",
                            Parameters.with("a", ordineDettaglio.getAnno()).and("s", ordineDettaglio.getSerie())
                                    .and("p", ordineDettaglio.getProgressivo())).project(String.class)
                    .singleResultOptional();
            if(optional.isPresent()) {
                intestazione = optional.get();
            }
            if(Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())){
                String campoUser5 = "VS.ART." + ordineDettaglio.getCodArtFornitore();
                String nota = "Riferimento n. " + ordineDettaglio.getAnno() + "/" + ordineDettaglio.getSerie() + "/" + ordineDettaglio.getProgressivo() + "-" + ordineDettaglio.getRigo();
                int update = OrdineFornitoreDettaglio.update("nota = :n, campoUser5 = :c, provenienza = :p, pid = :g " +
                                "WHERE progrGenerale = :pr",
                        Parameters.with("n", nota).and("c", StringUtils.truncate(campoUser5, 25))
                                .and("p", "C").and("pr", dto.getProgrGenerale())
                                .and("g", ordineDettaglio.getProgrGenerale()));
                if (update != 0) {
                    Log.debug("Collega OAF: Aggiornati " + update + " record");
                }

                OrdineFornitoreDettaglio rigoRif = mapper.createRigoRiferimento(dto.getSerie(), dto.getProgressivo(), intestazione, (dto.getRigo()+1),
                        (progressivoFornDettaglio+1), user);
                rigoRif.persist();

                OrdineFornitoreDettaglio.update("rigo = (rigo+1) WHERE anno = :anno AND serie = :serie" +
                                " AND progressivo = :progressivo and rigo >=:rigo",
                        Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                                .and("progressivo", dto.getProgressivo()).and("rigo", rigoRif.getRigo()));

                result.setError(Boolean.FALSE);
                result.setMsg("Articolo cliente collegato all'ordine a fornitore " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo());
                return result;
            } else {
                double qta = dto.getOquantita() - ordineDettaglio.getQuantita();
                OrdineFornitoreDettaglio.update("oQuantita = :q,oQuantitaV = :qv " +
                                "WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo =:r",
                        Parameters.with("anno", dto.getAnno())
                                .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo()).
                        and("q", qta).and("qv", qta).and("r", dto.getRigo()));

                Integer rigo = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(f.rigo) IS NULL THEN 0 ELSE MAX(f.rigo) END " +
                                        "FROM OrdineFornitoreDettaglio f " +
                                        " WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo ",
                                Parameters.with("anno", dto.getAnno())
                                        .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo()))
                        .project(Integer.class).singleResult();

                Optional<OrdineFornitoreDettaglio> singleResultOptional = OrdineFornitoreDettaglio.find("anno = :a AND serie = :s AND " +
                        "progressivo = :p AND rigo =:r", Parameters.with("a", dto.getAnno()).and("s", dto.getSerie())
                        .and("p", dto.getProgressivo()).and("r", dto.getRigo())).singleResultOptional();
                if(singleResultOptional.isPresent()){
                    OrdineFornitoreDettaglio o = singleResultOptional.get();
                    OrdineFornitoreDettaglio fornitoreDettaglio = mapper.copyEntity(o, progrGenerale, rigo, progressivoFornDettaglio, ordineDettaglio);
                    fornitoreDettaglio.persist();
                    OrdineFornitoreDettaglio rigoRif = mapper.createRigoRiferimento(dto.getSerie(), dto.getProgressivo(), intestazione, (rigo+1),
                            (progressivoFornDettaglio+1), user);
                    rigoRif.persist();
                }

            }

            result.setError(Boolean.FALSE);
            result.setMsg("Articolo cliente collegato all'ordine a fornitore " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo());
            return result;
        } catch (Exception e) {
            Log.error("Collega OAF: ERROR! ", e);
            result.setError(Boolean.FALSE);
            result.setMsg("Collega OAF: ERROR! " + e.getMessage());
            return result;
        }
    }
}
