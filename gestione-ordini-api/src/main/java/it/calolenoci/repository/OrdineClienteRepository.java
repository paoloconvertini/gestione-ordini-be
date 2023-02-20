package it.calolenoci.repository;

import it.calolenoci.entity.OrdineCliente;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.OrdineDettaglioId;
import it.calolenoci.entity.OrdineId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrdineClienteRepository extends CrudRepository<OrdineCliente, OrdineDettaglioId> {

    List<OrdineCliente> findByOrdineDettaglioIdAnnoAndOrdineDettaglioIdSerieAndOrdineDettaglioIdProgressivoOrderByOrdineDettaglioId(Integer anno, String serie, Integer progressivo);

}
