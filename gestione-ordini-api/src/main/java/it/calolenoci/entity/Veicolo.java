package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "GO_VEICOLO")
@Getter
@Setter
public class Veicolo extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "descrizione", nullable = false, length = 250)
    private String descrizione;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Veicolo veicolo = (Veicolo) o;

        if (id != veicolo.id) return false;
        if (descrizione != null ? !descrizione.equals(veicolo.descrizione) : veicolo.descrizione != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (descrizione != null ? descrizione.hashCode() : 0);
        return result;
    }
}
