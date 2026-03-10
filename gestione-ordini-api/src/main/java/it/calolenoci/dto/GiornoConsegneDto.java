package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
public class GiornoConsegneDto implements Serializable {

    private DayOfWeek giorno;

    private LocalDate data;

    private int numeroConsegne;

    private List<FasciaConsegneDto> fasce;

}
