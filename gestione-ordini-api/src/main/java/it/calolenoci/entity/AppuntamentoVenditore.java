package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GO_APPUNTAMENTO_VENDITORE")
@Getter
@Setter
public class AppuntamentoVenditore extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_APPUNTAMENTO")
    private Long idAppuntamento;

    @Column(name = "COD_VENDITORE")
    private String codVenditore;
}