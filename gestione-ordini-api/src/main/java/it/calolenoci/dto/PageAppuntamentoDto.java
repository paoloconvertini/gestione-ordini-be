package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageAppuntamentoDto implements Serializable {

    private long count;

    private List<AppuntamentoSearchDto> list;
}