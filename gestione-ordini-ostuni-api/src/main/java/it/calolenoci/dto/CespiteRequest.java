package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class CespiteRequest implements Serializable {
    private String id;
    private String tipoCespite;
    private Double perc;
    private String cespite;
    private Double importo;
    private String numDocAcq;
    private LocalDate dataAcq;
    private Boolean flPrimoAnno;
}
