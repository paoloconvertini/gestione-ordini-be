package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Primanota;
import it.calolenoci.mapper.PrimanotaMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrimanotaService {

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Inject
    PrimanotaMapper mapper;


    public List<PrimanotaDto> getById(FiltroPrimanota f) {
        return Primanota.find("select p.datamovimento, p.numerodocumento, p.causale, p.gruppoconto, p.sottoconto, p.descrsuppl, p.importo, p.progrgenerale, p.protocollo, p.anno, p.giornale, p.progrprimanota from Primanota p" +
                                " where p.giornale =:giornale AND p.anno =:anno AND p.protocollo =:protocollo",
                Parameters.with("giornale", f.getGiornale()).and("anno", f.getAnno()).and("protocollo", f.getProtocollo()))
                .project(PrimanotaDto.class).list();
    }

    @Transactional
    public void salva(PrimanotaDto dto) {
        try {
            Optional<Primanota> opt = Primanota.find("progrgenerale=:p",
                    Parameters.with("p", dto.getProgrgenerale())).firstResultOptional();
            if (opt.isEmpty()) {
                Log.debug("Sto aggiungendo un nuovo rigo alla primanota");
                Primanota p = Primanota.find("anno = :a AND giornale=:g AND protocollo=:p", Sort.descending("progrprimanota"),
                        Parameters.with("a", dto.getAnno()).and("g", dto.getGiornale())
                                .and("p", dto.getProtocollo())).firstResult();
                Integer progrGenerale = Primanota.find("SELECT MAX(progrgenerale) FROM Primanota").project(Integer.class).firstResult();
                Primanota primanota = mapper.buildPrimanota(dto, p, progrGenerale);
                primanota.persist();
            } else {
                Log.debug("Sto aggiornando la primanota");
                int update = Primanota.update("gruppoconto =:gruppo, sottoconto =:sotto, descrsuppl =:desc, importo=:i " +
                        "WHERE progrgenerale=:p", Parameters.with("gruppo", dto.getGruppoconto())
                        .and("desc", dto.getDescrsuppl())
                        .and("sotto", dto.getSottoconto())
                        .and("p", dto.getProgrgenerale())
                        .and("i", dto.getImporto())
                );
                boolean result = update > 0;
                if (result && (dto.getGiornale().equals("A") || StringUtils.equals("*CE", dto.getCausale()))) {
                    Log.debug("Primanota salva, creo un nuovo cespite...");
                    ammortamentoCespiteService.createCespite(dto);
                } else {
                    Log.debug("Primanota salva, Non è una creazione di un nuovo cespite!!");
                }
            }
        } catch (Exception e) {
            Log.error("Error saving primanota", e);
            throw e;
        }
    }

    @Transactional
    @TransactionConfiguration(timeout = 5000000)
    public void contabilizzaAmm(LocalDate date) throws Exception {
        Log.debug("### INIZIO contabilizzzione cespiti ###");
        try {
            String query = "SELECT c.tipoCespite, c.progressivo1, c.progressivo2, a.quota, a.quotaRivalutazione, " +
                    " t.costoGruppo, t.costoConto, t.ammGruppo, t.ammConto, " +
                    " t.fondoGruppo, t.fondoConto, t.plusGruppo, t.plusConto, t.minusGruppo, t.minusConto " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite t ON t.tipoCespite = c.tipoCespite " +
                    "WHERE a.anno =:a and c.attivo = 'T'";
            List<RegistroCespiteDto> cespiteDtos = Cespite.find(query, Parameters.with("a", date.getYear())).project(RegistroCespiteDto.class).list();
            Integer progrGenerale = Primanota.find("SELECT MAX(progrgenerale) + 1 FROM Primanota").project(Integer.class).firstResult();
            List<Primanota> primanotaList = Primanota.find("SELECT p " +
                    "FROM Primanota p " +
                    "where anno =:a AND giornale ='' ", Parameters.with("a", date.getYear())).list();
            int protocollo;
            if(primanotaList.isEmpty()){
                protocollo = 1;
            } else {
                protocollo = Primanota.find("SELECT MAX(protocollo) + 1 " +
                        "FROM Primanota " +
                        "where anno =:a AND giornale ='' ", Parameters.with("a", date.getYear())).project(Integer.class).firstResult();
            }
            Primanota.delete("datamovimento = :d AND causale = :c", Parameters.with("d", date).and("c", "GVM"));
            Map<String, List<RegistroCespiteDto>> mapTipoCespite = cespiteDtos.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getTipoCespite));
            List<Primanota> listToSave = new ArrayList<>();
            for (String tipoCespite : mapTipoCespite.keySet()) {
                List<RegistroCespiteDto> cespiteDBDtos = mapTipoCespite.get(tipoCespite);
                int rigo = 1;
                for (RegistroCespiteDto dto : cespiteDBDtos) {
                    listToSave.add(mapper.buildPrimanotaContabile("GVM", date, dto.getTipoCespite(), dto.getProgressivo1(), dto.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. Q.TA AMMORT. ORD.", dto.getAmmGruppo(), dto.getAmmConto(), dto.getQuota()));
                    listToSave.add(mapper.buildPrimanotaContabile("GVM", date, dto.getTipoCespite(),  dto.getProgressivo1(), dto.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORT. ORD.", dto.getFondoGruppo(), dto.getFondoConto(), -dto.getQuota()));
                    if(dto.getQuotaRivalutazione() != null){
                        listToSave.add(mapper.buildPrimanotaContabile("GVM", date, dto.getTipoCespite(), dto.getProgressivo1(), dto.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. Q.TA AMMORT. RIV.", dto.getAmmGruppo(), dto.getAmmConto(), dto.getQuotaRivalutazione()));
                        listToSave.add(mapper.buildPrimanotaContabile("GVM", date, dto.getTipoCespite(), dto.getProgressivo1(), dto.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORT. RIV.", dto.getFondoGruppo(), dto.getFondoConto(), -dto.getQuotaRivalutazione()));
                    }
                }
                protocollo++;
            }
            Primanota.persist(listToSave);
            Log.debug("### FINE contabilizzzione cespiti ###");
        } catch (Exception e) {
            Log.error("Error contabilizza ammortamenti", e);
        }
    }

    @Transactional
    public void registraVendita(VenditaCespiteDto venditaCespiteDto) throws Exception{
        try {
            List<Primanota> primanotaList = Primanota.find("anno = :a AND giornale=:g AND protocollo=:p",
                    Parameters.with("a", venditaCespiteDto.getAnno()).and("g", venditaCespiteDto.getGiornale())
                            .and("p", venditaCespiteDto.getProtocollo())).list();
            Optional<Primanota> optionalPrimanota = primanotaList.stream().filter(p ->
                    CategoriaCespite.find(" costoGruppo= :g AND costoConto =:c",
                            Parameters.with("g", p.getGruppoconto()).and("c", p.getSottoconto())).firstResultOptional().isPresent()
            ).findFirst();
            if(optionalPrimanota.isEmpty()){
                Log.error("Nessuna riga di prima nota trovata con conto cespite");
                throw new Exception("Nessuna riga di prima nota trovata con conto cespite");
            } else {
                Primanota dto = optionalPrimanota.get();
                String query = "SELECT c.id, c.tipoCespite, c.progressivo1, c.progressivo2, c.cespite, c.dataAcq, c.numDocAcq, c.fornitore, " +
                        "c.importo, c.importoRivalutazione, c.attivo, c.dataVendita, c.numDocVendita, c.intestatarioVendita, c.importoVendita," +
                        " c.superAmm, c.protocollo, c.giornale, c.anno, c.dataInizioCalcoloAmm, c.flPrimoAnno, c.fondoRivalutazione, " +
                        " t.id, t.descrizione, t.percAmmortamento, t.costoGruppo, t.costoConto, t.ammGruppo, t.ammConto, " +
                        "t.fondoGruppo, t.fondoConto, t.plusGruppo, t.plusConto, t.minusGruppo, t.minusConto " +
                        "FROM Cespite c " +
                        "JOIN CategoriaCespite t ON t.tipoCespite = c.tipoCespite " +
                        "WHERE c.id =:id ";
                Map<String, Object> params = new HashMap<>();
                params.put("id", venditaCespiteDto.getIdCespite());
                Optional<RegistroCespiteDto> optionalCespite = Cespite.find(query, params).project(RegistroCespiteDto.class).singleResultOptional();
                if(optionalCespite.isEmpty()){
                    Log.error("Error registra vendita: Cespite non trovato");
                    throw new RuntimeException("Error registra vendita: Cespite non trovato");
                }
                RegistroCespiteDto cespiteDto = optionalCespite.get();
                Integer progrGenerale = Primanota.find("SELECT MAX(progrgenerale) + 1 FROM Primanota").project(Integer.class).firstResult();
                List<Primanota> primanotas = Primanota.find("SELECT p " +
                        "FROM Primanota p " +
                        "where anno =:a AND giornale ='' ", Parameters.with("a", dto.getDatamovimento().getYear())).list();
                int protocollo;
                if(primanotas.isEmpty()){
                    protocollo = 1;
                } else {
                    protocollo = Primanota.find("SELECT MAX(protocollo) + 1 " +
                            "FROM Primanota " +
                            "where anno =:a AND giornale ='' ", Parameters.with("a", dto.getDatamovimento().getYear())).project(Integer.class).firstResult();
                }
                List<Primanota> listToSave = new ArrayList<>();
                int rigo = 1;
                Cespite cespite = Cespite.find("id =:id", Parameters.with("id", cespiteDto.getId())).firstResult();
                cespite.setDataVendita(dto.getDatamovimento());
                cespite.setNumDocVendita(dto.getNumerodocumento());
                cespite.setIntestatarioVendita(dto.getDescrsuppl());
                cespite.setImportoVendita(-dto.getImporto());
                cespite.persist();
                AmmortamentoCespite.delete("idAmmortamento =:id", Parameters.with("id", cespite.getId()));
                List<AmmortamentoCespite> ammortamentoCespiteList;
                if (cespite.getImportoRivalutazione() != null) {
                    ammortamentoCespiteList = ammortamentoCespiteService.ricalcoloRivalutato(cespiteDto, cespite.getDataVendita());
                } else {
                    ammortamentoCespiteList = ammortamentoCespiteService.ricalcoloCespite(cespiteDto, cespite.getDataVendita());
                }
                if(ammortamentoCespiteList.isEmpty()){
                    //TODO che si fa??
                } else {
                    AmmortamentoCespite.persist(ammortamentoCespiteList);
                    Optional<AmmortamentoCespite> optionalAmmortamentoCespite = ammortamentoCespiteList.stream().filter(a -> a.getAnno().equals(cespite.getDataVendita().getYear())
                            && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).findFirst();
                    if(optionalAmmortamentoCespite.isEmpty()){
                        optionalAmmortamentoCespite = ammortamentoCespiteList.stream()
                                .filter(a -> StringUtils.startsWith(a.getDescrizione(), "Ammortamento"))
                                .max(Comparator.comparing(AmmortamentoCespite::getAnno));
                    }
                    if(optionalAmmortamentoCespite.isPresent()){
                        AmmortamentoCespite a = optionalAmmortamentoCespite.get();
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. QUOTA AMMORTAMENTO", cespiteDto.getAmmGruppo(), cespiteDto.getAmmConto(), a.getQuota()));
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORTAMENTO", cespiteDto.getFondoGruppo(), cespiteDto.getFondoConto(), -a.getFondo()));
                        double plusMinus = cespite.getImportoVendita() - a.getResiduo();
                        Integer gruppoPlusMinus;
                        String contoPlusMinus;
                        if(plusMinus < 0){
                            //ho una minus
                            gruppoPlusMinus = cespiteDto.getMinusGruppo();
                            contoPlusMinus = cespiteDto.getMinusConto();
                        } else {
                            gruppoPlusMinus = cespiteDto.getPlusGruppo();
                            contoPlusMinus = cespiteDto.getMinusConto();
                        }
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo++, progrGenerale++,
                                "RIL. PLUS/MINUSVALENZA", gruppoPlusMinus, contoPlusMinus,
                                -plusMinus));
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo++, progrGenerale++,
                                "RIL. PLUS/MINUSVALENZA", cespiteDto.getCostoGruppo(), cespiteDto.getCostoConto(),
                                plusMinus));
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORTAMENTO", cespiteDto.getFondoGruppo(), cespiteDto.getFondoConto(), a.getFondo()));
                        listToSave.add(mapper.buildPrimanotaContabile("GVV", cespite.getDataVendita(), cespite.getTipoCespite(), cespite.getProgressivo1(), cespite.getProgressivo2(), protocollo, rigo, progrGenerale, "RIL. QUOTA AMMORTAMENTO", cespiteDto.getCostoGruppo(), cespiteDto.getCostoConto(), -a.getQuota()));
                        Primanota.persist(listToSave);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("Error contabilizza ammortamenti", e);
            throw e;
        }
    }
}
