package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ARTICOLI_TAB")
@Getter
@Setter
public class Articolo extends PanacheEntityBase {

    @Id
    @Column(length = 13)
    private String articolo;

    @Column(length = 50)
    private String descrArticolo;

    @Column(length = 40)
    private String descrArtSuppl;

    @Column(length = 5000, columnDefinition = "text")
    private String descrEstesa;

    @Column(length = 2)
    private String unitaMisura;

    @Column
    private Float costoBase;

    @Column
    private Float prezzoBase;

    @Column(length = 3)
    private String classeA1;
}
