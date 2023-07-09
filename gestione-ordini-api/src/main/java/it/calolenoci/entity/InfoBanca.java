package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "TEBA")
@Getter
@Setter
public class InfoBanca extends PanacheEntityBase {

    @Id
    @Column(length = 3, name="BANCAPRES")
    private String bancaPres;

    @Column(length = 40, name="DESCRBANCA2")
    private String descrBanca;

    @Column(name = "ABIBANCA")
    private Double abiBanca;
}
