package it.calolenoci.repository;

import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.OrdineId;
import org.springframework.data.repository.CrudRepository;

public interface OrdineTestatarepository extends CrudRepository<OrdineClienteTestata, OrdineId> {
}