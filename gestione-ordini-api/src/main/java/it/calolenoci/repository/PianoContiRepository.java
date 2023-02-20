package it.calolenoci.repository;

import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.OrdineId;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.PianoContiId;
import org.springframework.data.repository.CrudRepository;

public interface PianoContiRepository extends CrudRepository<PianoConti, PianoContiId> {
}
