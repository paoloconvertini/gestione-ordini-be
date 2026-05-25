package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "GO_ATTIVITA_MONTAGGIO")
@Getter
@Setter
public class AttivitaMontaggio extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ORDINE_ANNO")
    private Integer ordineAnno;

    @Column(name = "ORDINE_SERIE")
    private String ordineSerie;

    @Column(name = "ORDINE_PROGRESSIVO")
    private Integer ordineProgressivo;

    @Column(name = "NUMERO_ORDINE")
    private String numeroOrdine;

    @Column(name = "DATA_ORA_DA")
    private LocalDateTime dataOraDa;

    @Column(name = "DATA_ORA_A")
    private LocalDateTime dataOraA;

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

    @Column(name = "SCALA_MOBILE")
    private Boolean scalaMobile;

    @Column(name = "STATO")
    private String stato;

    @Column(name = "PROMEMORIA_INVIATO")
    private Boolean promemoriaInviato;

    @Column(name = "DATA_INVIO_PROMEMORIA")
    private LocalDateTime dataInvioPromemoria;

    @Column(name = "DATA_COMPLETAMENTO")
    private LocalDateTime dataCompletamento;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;
}