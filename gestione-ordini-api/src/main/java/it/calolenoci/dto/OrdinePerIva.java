package it.calolenoci.dto;

import lombok.*;

import jakarta.persistence.Column;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrdinePerIva implements Serializable {

    private  Integer anno;

    private String serie;

    private Integer progressivo;

    private String iva;

}
