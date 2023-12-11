package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.CespiteDBDto;
import it.calolenoci.dto.FiltroPrimanota;
import it.calolenoci.dto.PrimanotaDto;
import it.calolenoci.dto.VenditaCespiteDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Primanota;
import it.calolenoci.mapper.PrimanotaMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
                if (result && dto.getGiornale().equals("A")) {
                    ammortamentoCespiteService.createCespite(dto);
                }
            }
        } catch (Exception e) {
            Log.error("Error saving primanota", e);
            throw e;
        }
    }

    @Transactional
    public void contabilizzaAmm() throws Exception {
        try {
            String query = "SELECT c, a, cat " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite " +
                    "WHERE a.anno =:a and c.attivo = 'T'";
            List<CespiteDBDto> cespiteDtos = Cespite.find(query, Parameters.with("a", Year.now().getValue())).project(CespiteDBDto.class).list();
            Integer progrGenerale = Primanota.find("SELECT MAX(progrgenerale) + 1 FROM Primanota").project(Integer.class).firstResult();
            List<Primanota> primanotaList = Primanota.find("SELECT p " +
                    "FROM Primanota p " +
                    "where anno =:a AND giornale ='' ", Parameters.with("a", Year.now().getValue())).list();
            int protocollo;
            if(primanotaList.isEmpty()){
                protocollo = 1;
            } else {
                protocollo = Primanota.find("SELECT MAX(protocollo) + 1 " +
                        "FROM Primanota " +
                        "where anno =:a AND giornale ='' ", Parameters.with("a", Year.now().getValue())).project(Integer.class).firstResult();
            }
            Map<String, List<CespiteDBDto>> mapTipoCespite = cespiteDtos.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getTipoCespite()));
            List<Primanota> listToSave = new ArrayList<>();
            for (String tipoCespite : mapTipoCespite.keySet()) {
                List<CespiteDBDto> cespiteDBDtos = mapTipoCespite.get(tipoCespite);
                int rigo = 1;
                for (CespiteDBDto cespiteDto : cespiteDBDtos) {
                    listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++, "RIL. QUOTA AMMORTAMENTO", cespiteDto.getCategoria().getAmmGruppo(), cespiteDto.getCategoria().getAmmConto(), cespiteDto.getCespite().getImporto()));
                    listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORTAMENTO", cespiteDto.getCategoria().getFondoGruppo(), cespiteDto.getCategoria().getFondoConto(), -cespiteDto.getCespite().getImporto()));
                }
                protocollo++;
            }
            Primanota.persist(listToSave);
        } catch (Exception e) {
            Log.error("Error contabilizza ammortamenti", e);
            throw e;
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
                String query = "SELECT c, cat " +
                        "FROM Cespite c " +
                        "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite " +
                        "WHERE c.id =:id ";
                Map<String, Object> params = new HashMap<>();
                params.put("id", venditaCespiteDto.getIdCespite());
                Optional<CespiteDBDto> optionalCespite = Cespite.find(query, params).project(CespiteDBDto.class).singleResultOptional();
                if(optionalCespite.isEmpty()){
                    Log.error("Error registra vendita: Cespite non trovato");
                    throw new RuntimeException("Error registra vendita: Cespite non trovato");
                }
                CespiteDBDto cespiteDto = optionalCespite.get();
                Integer progrGenerale = Primanota.find("SELECT MAX(progrgenerale) + 1 FROM Primanota").project(Integer.class).firstResult();
                List<Primanota> primanotas = Primanota.find("SELECT p " +
                        "FROM Primanota p " +
                        "where anno =:a AND giornale ='' ", Parameters.with("a", Year.now().getValue())).list();
                int protocollo;
                if(primanotas.isEmpty()){
                    protocollo = 1;
                } else {
                    protocollo = Primanota.find("SELECT MAX(protocollo) + 1 " +
                            "FROM Primanota " +
                            "where anno =:a AND giornale ='' ", Parameters.with("a", Year.now().getValue())).project(Integer.class).firstResult();
                }
                List<Primanota> listToSave = new ArrayList<>();
                int rigo = 1;
                Cespite cespite = cespiteDto.getCespite();
                cespite.setDataVendita(dto.getDatamovimento());
                cespite.setNumDocVendita(dto.getNumerodocumento());
                cespite.setIntestatarioVendita(dto.getDescrsuppl());
                cespite.setImportoVendita(-dto.getImporto());
                cespite.persist();
                AmmortamentoCespite.delete("idAmmortamento =:id", Parameters.with("id", cespite.getId()));
                List<AmmortamentoCespite> ammortamentoCespiteList = ammortamentoCespiteService.calcoloSingoloCespite(cespite, cespite.getDataVendita());
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
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++, "RIL. QUOTA AMMORTAMENTO", cespiteDto.getCategoria().getAmmGruppo(), cespiteDto.getCategoria().getAmmConto(), a.getQuota()));
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORTAMENTO", cespiteDto.getCategoria().getFondoGruppo(), cespiteDto.getCategoria().getFondoConto(), -a.getFondo()));
                        double plusMinus = cespite.getImportoVendita() - a.getResiduo();
                        Integer gruppoPlusMinus;
                        String contoPlusMinus;
                        if(plusMinus < 0){
                            //ho una minus
                            gruppoPlusMinus = cespiteDto.getCategoria().getMinusGruppo();
                            contoPlusMinus = cespiteDto.getCategoria().getMinusConto();
                        } else {
                            gruppoPlusMinus = cespiteDto.getCategoria().getPlusGruppo();
                            contoPlusMinus = cespiteDto.getCategoria().getMinusConto();
                        }
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++,
                                "RIL. PLUS/MINUSVALENZA", gruppoPlusMinus, contoPlusMinus,
                                -plusMinus));
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++,
                                "RIL. PLUS/MINUSVALENZA", cespiteDto.getCategoria().getCostoGruppo(), cespiteDto.getCategoria().getCostoConto(),
                                plusMinus));
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo++, progrGenerale++, "RIL. FONDO AMMORTAMENTO", cespiteDto.getCategoria().getFondoGruppo(), cespiteDto.getCategoria().getFondoConto(), a.getFondo()));
                        listToSave.add(mapper.buildPrimanotaContabile(cespiteDto, protocollo, rigo, progrGenerale, "RIL. QUOTA AMMORTAMENTO", cespiteDto.getCategoria().getCostoGruppo(), cespiteDto.getCategoria().getCostoConto(), -a.getQuota()));
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
