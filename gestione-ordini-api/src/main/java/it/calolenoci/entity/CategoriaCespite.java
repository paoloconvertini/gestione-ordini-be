package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "TCTC")
@Getter
@Setter
public class CategoriaCespite extends PanacheEntityBase {

    @Id
    @Column(name = "TIPOCESPITE", nullable = false, length = 3)
    private String tipocespite;
    
    @Column(name = "DESCRTIPOCESP", nullable = true, length = 40)
    private String descrtipocesp;
    
    @Column(name = "DESCRSUPPLCES", nullable = true, length = 25)
    private String descrsupplces;
    
    @Column(name = "GRUPPOCESPITE", nullable = true)
    private Integer gruppocespite;
    
    @Column(name = "CONTOCESPITE", nullable = true, length = 6)
    private String contocespite;
    
    @Column(name = "GRUPPOFONDO", nullable = true)
    private Integer gruppofondo;
    
    @Column(name = "CONTOFONDO", nullable = true, length = 6)
    private String contofondo;
    
    @Column(name = "PCTPRIMOANNO", nullable = true)
    private Integer pctprimoanno;
    
    @Column(name = "PCTAMMORD", nullable = true, precision = 0)
    private Double pctammord;
    
    @Column(name = "GRUPPOAMM", nullable = true)
    private Integer gruppoamm;
    
    @Column(name = "CONTOAMM", nullable = true, length = 6)
    private String contoamm;
    
    @Column(name = "PCTAMMANT", nullable = true, precision = 0)
    private Double pctammant;
    
    @Column(name = "GRUPPOAMMANT", nullable = true)
    private Integer gruppoammant;
    
    @Column(name = "CONTOAMMANT", nullable = true, length = 6)
    private String contoammant;
    
    @Column(name = "VOCESPESA", nullable = true, length = 10)
    private String vocespesa;
    
    @Column(name = "PCTPRIMOANNOCIV", nullable = true)
    private Integer pctprimoannociv;
    
    @Column(name = "PCTAMMCIVILE", nullable = true, precision = 0)
    private Double pctammcivile;
    
    @Column(name = "GRUPPOMINUSVAL", nullable = true)
    private Integer gruppominusval;
    
    @Column(name = "CONTOMINUSVAL", nullable = true, length = 6)
    private String contominusval;
    
    @Column(name = "GRUPPOPLUSVAL", nullable = true)
    private Integer gruppoplusval;
    
    @Column(name = "CONTOPLUSVAL", nullable = true, length = 6)
    private String contoplusval;
    
    @Column(name = "SYS_CREATEDATE", nullable = true)
    private Date sysCreatedate;
    
    @Column(name = "SYS_CREATEUSER", nullable = true, length = 20)
    private String sysCreateuser;
    
    @Column(name = "SYS_UPDATEDATE", nullable = true)
    private Date sysUpdatedate;
    
    @Column(name = "SYS_UPDATEUSER", nullable = true, length = 20)
    private String sysUpdateuser;
}
