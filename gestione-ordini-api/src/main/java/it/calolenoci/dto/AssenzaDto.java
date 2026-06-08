package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AssenzaDto implements Serializable {

    private Long id;

    private String tipo;

    private LocalDate dataDa;
    private LocalDate dataA;

    private LocalTime oraDa;
    private LocalTime oraA;

    private Boolean giornataIntera;

    private String note;

    private List<String> codVenditori;
}