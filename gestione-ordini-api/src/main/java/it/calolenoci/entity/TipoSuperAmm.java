package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "GO_T_SUPER_AMMORT")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TipoSuperAmm extends PanacheEntity {

    @Column(name = "DESCRIZIONE", nullable = true, length = 100)
    private String descrizione;

    @Column(name = "PERC", nullable = true)
    private Integer perc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TipoSuperAmm that = (TipoSuperAmm) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
