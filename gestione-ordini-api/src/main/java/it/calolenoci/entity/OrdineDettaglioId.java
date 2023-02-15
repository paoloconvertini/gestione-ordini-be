package it.calolenoci.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;

@Getter
@Setter
public class OrdineDettaglioId implements Serializable {

    private static final long serialVersionUID = 4807686500687956806L;
    @Column(length = 4)
    private  Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;

    @Column
    private Integer rigo;
}
