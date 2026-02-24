package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "GO_LISTA_CARICO")
@Getter
@Setter
public class ListaCarichi extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "AZIENDA", nullable = false, length = 500)
    private String azienda;
    
    @Column(name = "NUMERO_ORDINE", nullable = false, length = 50)
    private String numeroOrdine;

    @Column(name = "DEPOSITO")
    private Long deposito;
    
    @Column(name = "DATA_DISPONIBILE", nullable = true)
    private LocalDate dataDisponibile;
    
    @Column(name = "PESO", nullable = true, precision = 0)
    private Double peso;
    
    @Column(name = "TRASPORTATORE")
    private Long trasportatore;
    
    @Column(name = "DT_CONVALIDA", nullable = true)
    private LocalDate dataConvalida;

    @Column(name = "NUM_CONVALIDA", nullable = true)
    private Long numeroConvalida;
}
