package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "GO_ORDINE_FORNITORE")
@IdClass(FornitoreId.class)
@Getter
@Setter
public class GoOrdineFornitore extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLINVIATO", columnDefinition = "CHAR(1)")
    private Boolean flInviato;

    @Column(length = 2000, name = "NOTE")
    private String note;

    @Column(length = 100, name= "DATAINVIO")
    private Date dataInvio;

}
