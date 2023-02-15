/*
package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Entity
@Table(name = "ARTICOLO")
@NoArgsConstructor
@AllArgsConstructor
public class Articolo extends PanacheEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 200)
    private String codice;

    @Column(length = 2000)
    private String descrizione;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private OrdineCliente idOrdine;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Articolo articolo = (Articolo) o;
        return Objects.equals(id, articolo.id) && Objects.equals(codice, articolo.codice);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


    public static List<Articolo> findByOrdineId(Long id) {
        return find("idOrdine =:idOrdine", Parameters.with("idOrdine", id).map()).list();
    }
}
*/
