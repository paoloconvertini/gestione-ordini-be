package it.calolenoci.dto;

import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggiornaDataDto implements Serializable {

    private Integer pid;

    private Double settimana;

    private Date dataConsegna;

}
