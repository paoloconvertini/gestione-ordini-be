package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class FiltroAppuntamentoDto implements Serializable {

    private Long sedeId;

    private LocalDate dataDa;
    private LocalDate dataA;

    private String nomeCliente;

    private String codVenditore;

    private Long motivoId;

}