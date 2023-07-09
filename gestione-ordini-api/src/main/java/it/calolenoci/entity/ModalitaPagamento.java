package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "TGCP")
@Getter
@Setter
public class ModalitaPagamento extends PanacheEntityBase {

    @Id
    @Column(length = 3, name="CODICEPAGAMENTO")
    private String codice;

    @Column(name="DESCRCP", length = 40)
    private String descrizione;
}
