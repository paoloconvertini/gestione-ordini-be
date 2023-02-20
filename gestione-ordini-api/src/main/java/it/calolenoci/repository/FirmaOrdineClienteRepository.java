package it.calolenoci.repository;

import it.calolenoci.entity.FirmaOrdineCliente;
import it.calolenoci.entity.OrdineId;
import org.springframework.data.repository.CrudRepository;

public interface FirmaOrdineClienteRepository extends CrudRepository<FirmaOrdineCliente, OrdineId> {
}
