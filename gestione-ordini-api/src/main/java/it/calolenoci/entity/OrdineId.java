package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class OrdineId implements Serializable {

    @Serial
    private static final long serialVersionUID = 4807686500687956806L;
    @Column(length = 4)
    private  Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;

    @Override
    public String toString() {
        return anno + "_" + serie + "_" + progressivo;
    }
}
