package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Primanota;
import it.calolenoci.entity.TipoSuperAmm;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrimanotaService {

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;




    public List<PrimanotaDto> getById(FiltroPrimanota f) {
        return Primanota.find("select p.datamovimento, p.numerodocumento, p.causale, p.gruppoconto, p.sottoconto, p.descrsuppl, p.importo, p.progrgenerale, p.protocollo, p.anno, p.giornale from Primanota p" +
                                " where p.giornale =:giornale AND p.anno =:anno AND p.protocollo =:protocollo",
                Parameters.with("giornale", f.getGiornale()).and("anno", f.getAnno()).and("protocollo", f.getProtocollo()))
                .project(PrimanotaDto.class).list();
    }

    @Transactional
    public boolean save(PrimanotaDto dto) {
        int update = Primanota.update("gruppoconto =:gruppo, sottoconto =:sotto, descrsuppl =:desc " +
                "WHERE progrgenerale=:p", Parameters.with("gruppo", dto.getGruppoconto())
                .and("desc", dto.getDescrsuppl())
                .and("sotto", dto.getSottoconto())
                .and("p", dto.getProgrgenerale()));
        boolean result = update > 0;
        if (result) {
            ammortamentoCespiteService.createCespite(dto);
        }
        return  result;
    }
}
