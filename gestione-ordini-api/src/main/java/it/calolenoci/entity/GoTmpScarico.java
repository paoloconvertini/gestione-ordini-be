package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "GO_TMP_SCARICO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoTmpScarico extends PanacheEntityBase {

    @EmbeddedId
    private GoTmpScaricoPK id;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(name = "ATTIVO", columnDefinition = "CHAR(1)", length = 1)
    private Boolean attivo;
    
}
