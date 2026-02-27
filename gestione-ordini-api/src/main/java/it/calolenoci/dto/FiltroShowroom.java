package it.calolenoci.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FiltroShowroom {

    private LocalDate dataDa;
    private LocalDate dataA;

    private Long venditoreId;

    private String nomeCliente;

    private String provincia;
    private String comuneIstat;

    private int page;
    private int size;

    private Long sedeId;

}