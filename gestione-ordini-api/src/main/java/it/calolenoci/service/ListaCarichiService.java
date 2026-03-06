package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.ListaCarichi;
import it.calolenoci.entity.Ordine;
import it.calolenoci.mapper.ListaCarichiMapper;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

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

    public List<ListaCarichiDto> findCarichiInviati(FiltroCarichi filtroCarichi) {
        String query = "SELECT l.id, l.azienda, l.numeroOrdine, d.id, d.nome, l.dataDisponibile, l.peso, " +
                "t.id, t.nome, " +
                "l.dataConvalida, l.numeroConvalida " +
                "FROM ListaCarichi l " +
                "LEFT JOIN Deposito d ON d.id = l.deposito " +
                "LEFT JOIN Trasportatore t ON t.id = l.trasportatore " +
                "WHERE l.numeroConvalida is not null " +
                "AND l.dataConvalida = :dt AND l.numeroConvalida = :n";
        Map<String, Object> params = new HashMap<>();
        params.put("dt", filtroCarichi.getDataConvalida());
        params.put("n", filtroCarichi.getNumeroConvalida());
        return ListaCarichi.find(query, params).project(ListaCarichiDto.class).list();
    }

    @Transactional
    public boolean salvaCarico(ListaCarichiDto dto) {
        //edit
        if(dto.getId() != null) {
            ListaCarichi carico = ListaCarichi.findById(dto.getId());
            if (!Objects.equals(carico.getNumeroOrdine(), dto.getNumeroOrdine())) {
                Optional<ListaCarichi> optCarico = ListaCarichi.find("numeroOrdine = :n",
                        Parameters.with("n", dto.getNumeroOrdine())).firstResultOptional();
                if (optCarico.isPresent()) {
                    return false;
                }
            }
            ListaCarichi.update("azienda =:a, numeroOrdine = :n, deposito =:d, " +
                            "dataDisponibile =:dt, peso =:p, trasportatore = :t " +
                            "WHERE id=:id",
                    Parameters.with("a", dto.getAzienda())
                            .and("n", dto.getNumeroOrdine()).and("d", dto.getIdDeposito())
                            .and("dt", dto.getDataDisponibile()).and("p", dto.getPeso())
                            .and("t", dto.getIdTrasportatore())
                            .and("id", dto.getId()));
        // nuovo
        } else {
            Optional<ListaCarichi> optCarico = ListaCarichi.find("numeroOrdine = :n",
                    Parameters.with("n", dto.getNumeroOrdine())).firstResultOptional();
            if (optCarico.isPresent()) {
                return false;
            }
            ListaCarichi.persist(mapper.fromDtoToEntity(dto));
        }
        return true;
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

        Optional<Long> optional = ListaCarichi.find(
                "select COALESCE(MAX(numeroConvalida),0) from ListaCarichi where dataConvalida = :d",
                Parameters.with("d", LocalDate.now())
        ).project(Long.class).firstResultOptional();

        Long progressivoGiorno = optional.map(v -> v + 1).orElse(1L);

        List<Long> ids = list.stream()
                .map(ListaCarichiDto::getId)
                .toList();

        ListaCarichi.update(
                "dataConvalida = :d, numeroConvalida = :p WHERE id IN (:ids)",
                Parameters.with("d", LocalDate.now())
                        .and("p", progressivoGiorno)
                        .and("ids", ids)
        );

        String nomeFile = LocalDate.now() + "_" + progressivoGiorno + ".pdf";

        try {
            jasperService.createReport(list, nomeFile);
        } catch (JRException | IOException e) {
            Log.error("Errore nella creazione del report per la lista di carico ", e);
        }

        long fine = System.currentTimeMillis();
        Log.info("Fine creaReport: " + (fine - inizio) / 1000 + " sec");

        return nomeFile;
    }

    public List<ListaCarichiDto> findConvalide(FiltroCarichi filtroCarichi) {
        String query = "SELECT distinct l.dataConvalida, l.numeroConvalida " +
                "FROM ListaCarichi l " +
                "WHERE l.numeroConvalida is not null ";
        Map<String, Object> params = new HashMap<>();
        if(filtroCarichi.getDataConvalida() != null){
            params.put("dt", filtroCarichi.getDataConvalida());
            query += "AND l.dataConvalida = :dt ";
        }
        if(StringUtils.isNotEmpty(filtroCarichi.getFornitore())){
            params.put("f", "%" + filtroCarichi.getFornitore() + "%");
            query += "AND l.azienda LIKE :f ";
        }
        if(StringUtils.isNotEmpty(filtroCarichi.getNumeroOrdine())){
            params.put("n", "%" + filtroCarichi.getNumeroOrdine() + "%");
            query += "AND l.numeroOrdine LIKE :n ";
        }
        query += " ORDER BY l.dataConvalida desc";
        return ListaCarichi.find(query, params).project(ListaCarichiDto.class).list();
    }
}
