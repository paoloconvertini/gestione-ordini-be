package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ConsegnaGiornalieraDto implements Serializable {
    private List<OrdineDTO> consegne;
}
