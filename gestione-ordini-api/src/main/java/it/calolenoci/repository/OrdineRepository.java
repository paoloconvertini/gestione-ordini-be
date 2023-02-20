package it.calolenoci.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineId;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrdineRepository implements PanacheRepositoryBase<Ordine, OrdineId> {
}
