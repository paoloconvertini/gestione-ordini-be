package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "GO_ORD_VEICOLO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoOrdVeicolo extends PanacheEntityBase {

    @EmbeddedId
    private GoOrdVeicoloPK id;
}
