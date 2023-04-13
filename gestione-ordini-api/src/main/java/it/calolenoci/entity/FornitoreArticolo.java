package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "FORNALTERNATIVI")
@Getter
@Setter
public class FornitoreArticolo extends PanacheEntityBase {

    @EmbeddedId
    private FornitoreArticoloId fornitoreArticoloId;

}
