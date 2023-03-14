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

@ApplicationScoped
public class FatturaService {

    public List<FatturaDto> getBolle(){
        List<FatturaDto> resultList = FattureDettaglio
                .find("Select f2.progrOrdCli from FattureDettaglio f2 INNER JOIN Fatture f ON" +
                        " f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo" +
                        " WHERE f2.progrOrdCli <> 0 ")
                .project(FatturaDto.class)
                .list();
        if(resultList.isEmpty()){
            return null;
        }
        return resultList;
    }

}
