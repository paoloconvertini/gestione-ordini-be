package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "GO_QUADRATURA_CESPITE")
@Getter
@Setter
public class QuadraturaCespite extends PanacheEntityBase {
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;
    
    @Column(name = "ID_CESPITE", nullable = false, length = 36)
    private String idCespite;
    
    @Column(name = "ANNO", nullable = false)
    private int anno;
    
    @Column(name = "AMMORTAMENTO", nullable = false, precision = 0)
    private double ammortamento;

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (idCespite != null ? idCespite.hashCode() : 0);
        result = 31 * result + anno;
        temp = Double.doubleToLongBits(ammortamento);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
