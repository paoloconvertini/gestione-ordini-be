package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link it.calolenoci.entity.NotaConsegna} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaConsegnaDto implements Serializable {
    private String id;
    private LocalDate dataNota;
    private String nota;
}