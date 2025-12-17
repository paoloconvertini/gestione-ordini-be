package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "GO_AUDIT")
@Getter
@Setter
public class Audit extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ENTITY_NAME", length = 50, nullable = false)
    private String entityName;

    @Column(name = "ANNO", nullable = false)
    private Integer anno;

    @Column(name = "SERIE", length = 3, nullable = false)
    private String serie;

    @Column(name = "PROGRESSIVO", nullable = false)
    private Integer progressivo;

    @Column(name = "RIGO")
    private Integer rigo;

    @Column(name = "PROGR_GENERALE")
    private Integer progrGenerale;

    @Column(name = "FIELD_NAME", length = 50, nullable = false)
    private String fieldName;

    @Column(name = "OLD_VALUE", length = 2000)
    private String oldValue;

    @Column(name = "NEW_VALUE", length = 2000)
    private String newValue;

    @Column(name = "ACTION_TYPE", length = 30, nullable = false)
    private String actionType;

    @Column(name = "OPERATION_SOURCE", length = 100, nullable = false)
    private String operationSource;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate = new Date();

    @Column(name = "NOTE", length = 500)
    private String note;
}
