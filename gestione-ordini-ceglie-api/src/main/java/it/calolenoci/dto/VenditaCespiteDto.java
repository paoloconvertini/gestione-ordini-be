package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenditaCespiteDto implements Serializable {

    private String idCespite;

    private Integer protocollo;

    private String giornale;

    private Integer anno;


}
