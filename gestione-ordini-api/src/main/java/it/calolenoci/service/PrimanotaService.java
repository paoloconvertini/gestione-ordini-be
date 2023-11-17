package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Primanota;
import it.calolenoci.entity.TipoSuperAmm;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import it.calolenoci.mapper.PrimanotaMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
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
                if (result) {
                    ammortamentoCespiteService.createCespite(dto);
                }
            }
        } catch (Exception e) {
            Log.error("Error saving primanota", e);
            throw e;
        }
    }
}
