package it.calolenoci.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomVisitDto {

    private Long id;
    private String nomeCliente;
    private String codiceCliente;
    private String comuneIstat;      // codice salvato
    private String comuneNome;       // valorizzato via join TTCO
    private String provinciaSigla;   // valorizzato via join TTCO
    private String telefono;
    private Long motivoId;
    private String motivoDescrizione;
    private Long motivoParentId;
    private String motivoParentDescrizione;
    private String venditoreCodice;
    private String venditoreNome; // valorizzato lato service
    private LocalDateTime dataVisita;
    private Long sedeId;
    private String sedeDescrizione;
}
