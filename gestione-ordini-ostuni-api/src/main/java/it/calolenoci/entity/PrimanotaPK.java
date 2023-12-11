package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PrimanotaPK implements Serializable {

    @Column
    private Integer anno;

    @Column
    private String giornale;

    @Column
    private Integer protocollo;

    @Column
    private Integer progrprimanota;

}
