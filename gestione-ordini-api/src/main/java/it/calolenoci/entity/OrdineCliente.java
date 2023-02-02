package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = "ORDINE_CLIENTE")
@NoArgsConstructor
@AllArgsConstructor
public class OrdineCliente extends PanacheEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 200)
    private String numeroOrdine;

    @Column(length = 2000)
    private String cliente;

    @Column
    private Date dataOrdine;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdineCliente articolo = (OrdineCliente) o;
        return Objects.equals(id, articolo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
