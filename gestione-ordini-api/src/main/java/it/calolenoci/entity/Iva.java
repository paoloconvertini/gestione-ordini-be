package it.calolenoci.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "TGCI")
public class Iva {
    @Id
    @Column(name = "CODICEIVA", nullable = false, length = 3)
    private String codiceIva;

    @Column(name = "DESCRCI", length = 20)
    private String descrci;

    @Column(name = "DESCRESTESA", length = 60)
    private String descrEstesa;

    @Column(name = "ANNOTAZIONI", length = 150)
    private String annotazioni;

    @Column(name = "ALIQUOTA")
    private Double aliquota;

    @Column(name = "PCTINDET")
    private Integer pctindet;

    @Column(name = "VENTILA", length = 1)
    private String ventila;

    @Column(name = "DICHIVA", length = 1)
    private String dichIva;

    @Column(name = "OPERAZIONEIVA", length = 3)
    private String operazioneIva;

    @Column(name = "TIPO", length = 2)
    private String tipo;

    @Column(name = "FLINTRA", length = 1)
    private String flintra;

    @Column(name = "REGSPECIALE", length = 3)
    private String regSpeciale;

    @Column(name = "IVAVENTILAZIONE", length = 3)
    private String ivaVentilazione;

    @Column(name = "ALIQUOTASCORPORO")
    private Integer aliquotaScorporo;

    @Column(name = "FLBOLLOESENTE", length = 1)
    private String flBolloEsente;

    @Column(name = "FENATURA", length = 4)
    private String feNatura;

    @Column(name = "FEREGIME", length = 4)
    private String feRegime;

    @Column(name = "CAMPOUSER", length = 60)
    private String campoUser;

    @Column(name = "DICHIVAANNUALE", length = 1)
    private String dichIvaAnnuale;

    @Column(name = "FLBLACKLIST", length = 3)
    private String flblacklist;

    @Column(name = "NATURA_FATTPA", length = 2)
    private String naturaFattpa;

    @Column(name = "SYS_CREATEDATE")
    private Instant sysCreatedate;

    @Column(name = "SYS_CREATEUSER", length = 20)
    private String sysCreateuser;

    @Column(name = "SYS_UPDATEDATE")
    private Instant sysUpdatedate;

    @Column(name = "SYS_UPDATEUSER", length = 20)
    private String sysUpdateuser;

    @Column(name = "FETD", length = 4)
    private String fetd;

    @Column(name = "FERIFNORMA", length = 40)
    private String ferifnorma;

    @Column(name = "FEDATIGEST", length = 40)
    private String fedatigest;


}