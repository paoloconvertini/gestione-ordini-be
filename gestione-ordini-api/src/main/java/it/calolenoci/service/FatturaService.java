package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.entity.Fatture;
import it.calolenoci.entity.FattureDettaglio;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.DoubleStream;

@ApplicationScoped
public class FatturaService {

    public List<FatturaDto> getBolle(){
        return FattureDettaglio
                .find("Select f.progrOrdCli, SUM(f.quantita) as qta FROM FattureDettaglio f WHERE f.progrOrdCli <> 0 GROUP BY f.progrOrdCli ")
                .project(FatturaDto.class)
                .list();
    }

    public List<FatturaDto> getBolle(Integer progrCliente){
        return FattureDettaglio
                .find("Select f.numeroBolla, f.dataBolla, f2.quantita as qta FROM FattureDettaglio f2 " +
                        "INNER JOIN Fatture f ON f.anno = f2.anno and f.serie = f2.serie and f.progressivo = f2.progressivo " +
                        " WHERE f2.progrOrdCli = :progCliente", Parameters.with("progCliente", progrCliente))
                .project(FatturaDto.class)
                .list();
    }

}
