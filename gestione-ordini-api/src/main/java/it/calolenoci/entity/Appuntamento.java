package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "GO_APPUNTAMENTO")
@Getter
@Setter
public class Appuntamento extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEDE_ID", nullable = false)
    private Sede sede;

    @Column(name = "GRUPPO_CONTO")
    private Integer gruppoConto;

    @Column(name = "SOTTO_CONTO")
    private String sottoConto;

    @Column(name = "NOME_CLIENTE")
    private String nomeCliente;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "VIA")
    private String via;

    @Column(name = "CIVICO")
    private String civico;

    @Column(name = "CAP")
    private String cap;

    @Column(name = "COMUNE")
    private String comune;

    @Column(name = "PROVINCIA")
    private String provincia;

    @Column(name = "DATA_APPUNTAMENTO")
    private LocalDate dataAppuntamento;

    @Column(name = "ORA_DA")
    private LocalTime oraDa;

    @Column(name = "ORA_A")
    private LocalTime oraA;

    @Column(name = "COD_VENDITORE")
    private String codVenditore;

    @Column(name = "ID_MOTIVO")
    private Long idMotivo;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "PROMEMORIA_INVIATO")
    private Boolean promemoriaInviato;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;
}