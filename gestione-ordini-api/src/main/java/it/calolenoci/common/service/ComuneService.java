package it.calolenoci.common.service;

import it.calolenoci.common.entity.Comune;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class ComuneService {

    @PersistenceContext(unitName = "common")
    EntityManager em;

    public List<Comune> findByCodici(Collection<String> codici) {
        return em.createQuery(
                        "FROM Comune c WHERE c.codiceIstat IN :codici",
                        Comune.class
                )
                .setParameter("codici", codici)
                .getResultList();
    }

    public List<String> findProvince() {

        return em.createQuery(
                "SELECT DISTINCT c.siglaProvincia FROM Comune c ORDER BY c.siglaProvincia",
                String.class
        ).getResultList();
    }

    public List<Comune> searchComuni(String provincia, String testo) {
        if (testo == null || testo.length() < 3) {
            return List.of();
        }
        String query = "FROM Comune c WHERE 1=1 ";

        if (provincia != null && !provincia.isBlank()) {
            query += " AND c.siglaProvincia = :prov ";
        }

        if (!testo.isBlank()) {
            query += " AND UPPER(c.nomeComune) LIKE :nome ";
        }

        query += " ORDER BY c.nomeComune";

        var q = em.createQuery(query, Comune.class);

        if (provincia != null && !provincia.isBlank()) {
            q.setParameter("prov", provincia);
        }

        if (testo != null && !testo.isBlank()) {
            q.setParameter("nome", "%" + testo.toUpperCase() + "%");
        }

        q.setMaxResults(20); // limite autocomplete

        return q.getResultList();
    }
}
