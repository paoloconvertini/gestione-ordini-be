package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DashboardCaricoDto {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String cliente;
    private LocalDate dataCarico;
    private Long giorni;
}
