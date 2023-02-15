package it.calolenoci.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class OrdineId implements Serializable {

    private static final long serialVersionUID = 4807686500687956806L;
    @Column(length = 4)
    private  Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;
}
