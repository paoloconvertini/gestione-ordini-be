package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageOrdineDto implements Serializable {
    private long count;

    private List<OrdineDTO> list;
}
