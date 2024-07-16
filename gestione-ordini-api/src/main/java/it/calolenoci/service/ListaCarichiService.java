package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.ListaCarichi;
import it.calolenoci.entity.Ordine;
import it.calolenoci.mapper.ListaCarichiMapper;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ListaCarichiService {

    @Inject
    ListaCarichiMapper mapper;

    @Inject
    JasperService jasperService;

    public List<ListaCarichiDto> findCarichi(FiltroCarichi filtroCarichi) {
        String query = "SELECT l.id, l.azienda, l.numeroOrdine, d.id, d.nome, l.dataDisponibile, l.peso, " +
                "t.id, t.nome, " +
                "l.dataConvalida, l.numeroConvalida " +
                "FROM ListaCarichi l " +
                "LEFT JOIN Deposito d ON d.id = l.deposito " +
                "LEFT JOIN Trasportatore t ON t.id = l.trasportatore ";
        if ("0".equals(filtroCarichi.getInviato())) {
            query += "WHERE l.numeroConvalida is null";
        } else {
            query += "WHERE l.numeroConvalida is not null";
        }
        Map<String, Object> params = new HashMap<>();
        if(filtroCarichi.getDataDisponibile() != null){
            query += " AND l.dataDisponibile = :dt";
            params.put("dt", filtroCarichi.getDataDisponibile());
        }
        return ListaCarichi.find(query, params).project(ListaCarichiDto.class).list();
    }

    @Transactional
    public void salvaCarico(ListaCarichiDto dto) {
        if (dto.getId() != null) {
            ListaCarichi.update("azienda =:a, numeroOrdine = :n, deposito =:d, " +
                            "dataDisponibile =:dt, peso =:p, trasportatore = :t " +
                            "WHERE id=:id",
                    Parameters.with("a", dto.getAzienda())
                            .and("n", dto.getNumeroOrdine()).and("d", dto.getIdDeposito())
                            .and("dt", dto.getDataDisponibile()).and("p", dto.getPeso())
                            .and("t", dto.getIdTrasportatore())
                            .and("id", dto.getId()));
        } else {
            ListaCarichi.persist(mapper.fromDtoToEntity(dto));
        }
    }

    public ListaCarichiDto getCarico(Long id) {
        String query = "SELECT l.id, l.azienda, l.numeroOrdine, d.id, d.nome, l.dataDisponibile, l.peso, " +
                "t.id, t.nome, " +
                "l.dataConvalida, l.numeroConvalida " +
                "FROM ListaCarichi l " +
                "LEFT JOIN Deposito d ON d.id = l.deposito " +
                "LEFT JOIN Trasportatore t ON t.id = l.trasportatore " +
                "WHERE l.id = :id";
        return ListaCarichi.find(query, Parameters.with("id", id)).project(ListaCarichiDto.class).firstResultOptional().orElseThrow();
    }

    @Transactional
    public String creaReport(List<ListaCarichiDto> list) {
        long inizio = System.currentTimeMillis();

        Optional<Long> optional = ListaCarichi.find("select ISNULL(numeroConvalida, 0) from ListaCarichi where dataConvalida  = :d", Parameters.with("d", LocalDate.now()))
                .project(Long.class).firstResultOptional();
        Long progressivoGiorno;
        if(optional.isEmpty()){
            progressivoGiorno = 1L;
        } else {
            progressivoGiorno = optional.get();
            progressivoGiorno += 1L;
        }
        Long finalProgressivoGiorno = progressivoGiorno;
        list.forEach(l -> ListaCarichi.update("dataConvalida =:d, numeroConvalida = :p WHERE id =:id",
                Parameters.with("d", LocalDate.now()).and("p", finalProgressivoGiorno).and("id", l.getId())));
        String nomeFile = LocalDate.now() + "_" + (finalProgressivoGiorno) + ".pdf";
        try {
            jasperService.createReport(list, nomeFile);
        } catch (JRException | IOException e) {
            Log.error("Errore nella creazione del report per la lista di carico ", e);
        }

        long fine = System.currentTimeMillis();
        Log.info("Fine creaReport: " + (fine - inizio) / 1000 + " sec");
        return nomeFile;
    }
}
