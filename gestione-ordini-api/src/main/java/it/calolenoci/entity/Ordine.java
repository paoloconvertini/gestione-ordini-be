package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "ORDCLI")
@IdClass(OrdineId.class)
@Getter
@Setter
public class Ordine extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Column
    private Integer gruppoCliente;

    @Column(length = 6)
    private String contoCliente;

    @Column(length = 80)
    private String riferimento;

    @Column
    private Integer gruppoFattura;

    @Column(length = 6)
    private String contoFattura;

    @Column(length = 1)
    private String tipoFattura;

    @Column(length = 1)
    private String provvisorio;

    @Column(name= "BANCAAPPOGGIO")
    private Double bancaAppoggio;

    @Column(name= "NSBANCAINCASSO", length = 3)
    private String nsBancaIncasso;


    @Column(name = "NUMEROCOLLI")
    private Integer numerocolli;

    @Column(name = "FCODDIFFPAG")
    private String fcoddiffpag;

    @Column(name = "FOGGETTO")
    private String oggetto;

    @Column(name = "SPESEBOLLO")
    private String spesebollo;

    @Column(name = "IVAPRIMASCAD")
    private String ivaprimascad;

    @Column(name = "AGENTE")
    private String agente;

    @Column(name = "LISTINO")
    private String listino;

    @Column(name = "MODOCONSEGNA")
    private String modoconsegna;

    @Column
    private LocalDate dataOrdine;

    @Column
    private LocalDate dataRichiesta;

    @Column(length = 15)
    private String numeroConferma;

    @Column
    private LocalDate dataConferma;

    @Column
    private LocalDate dataConfermaCli;

    @Column(name = "FCODICEPAGAMENT", length = 3)
    private String codicePagamento;

    @Column(name = "INTESTDIVERSE")
    private String intestdiverse;

    @Column(name = "INDIRDIVERSE")
    private String indirdiverse;

    @Column(name = "LOCDIVERSE")
    private String locdiverse;

    @Column(name = "CAPDIVERSE")
    private String capdiverse;

    @Column(name = "PROVDIVERSE")
    private String provdiverse;

    @Column(name = "SYS_CREATEDATE")
    private LocalDateTime createDate;

    @Column(name = "SYS_UPDATEDATE")
    private LocalDateTime updateDate;

    @Column(name = "ID_ORDCLI", insertable = false, updatable = false)
    private Integer id;

    public static Ordine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }



}
