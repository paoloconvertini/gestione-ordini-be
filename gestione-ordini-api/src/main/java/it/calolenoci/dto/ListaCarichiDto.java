package it.calolenoci.dto;

import it.calolenoci.entity.Deposito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link it.calolenoci.entity.ListaCarichi} entity
 */
@Data
public class ListaCarichiDto implements Serializable {
    private Long id;
    private String azienda;
    private String numeroOrdine;
    private Long idDeposito;

    private String deposito;
    private LocalDate dataDisponibile;
    private Double peso;
    private Long idTrasportatore;

    private String trasportatore;

    private LocalDate dataConvalida;
    private Long numeroConvalida;

    public ListaCarichiDto() {
    }

    // findCarichi
    public ListaCarichiDto(Long id, String azienda, String numeroOrdine, Long idDeposito,
                           String deposito, LocalDate dataDisponibile, Double peso,
                           Long idTrasportatore, String trasportatore, LocalDate dataConvalida, Long numeroConvalida) {
        this.id = id;
        this.azienda = azienda;
        this.numeroOrdine = numeroOrdine;
        this.idDeposito = idDeposito;
        this.deposito = deposito;
        this.dataDisponibile = dataDisponibile;
        this.peso = peso;
        this.idTrasportatore = idTrasportatore;
        this.trasportatore = trasportatore;
        this.dataConvalida = dataConvalida;
        this.numeroConvalida = numeroConvalida;
    }
}