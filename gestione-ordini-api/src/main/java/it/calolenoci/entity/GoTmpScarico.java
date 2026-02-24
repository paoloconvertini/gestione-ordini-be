package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.converter.TrueFalseConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;

@Entity
@Table(name = "GO_TMP_SCARICO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoTmpScarico extends PanacheEntityBase {

    @EmbeddedId
    private GoTmpScaricoPK id;

    @Convert(converter = TrueFalseConverter.class)
    @Column(name = "ATTIVO", columnDefinition = "CHAR(1)", length = 1)
    private Boolean attivo;
    
}
