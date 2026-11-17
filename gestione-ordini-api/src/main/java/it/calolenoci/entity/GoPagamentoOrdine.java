package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "GO_PAGAMENTO_ORDINE")
@Getter
@Setter
public class GoPagamentoOrdine extends PanacheEntityBase {
    @Id @Column(name = "PROGRGENERALE")
    private Integer progrGenerale;
    @Column(name = "ANNO")
    private Integer anno;
    @Column(name = "SERIE", length = 3)
    private String serie;
    @Column(name = "PROGRESSIVO")
    private Integer progressivo;
    @Column(name = "RIGO")
    private Integer rigo;
    @Column(name = "ARTICOLO", length = 13)
    private String articolo;
    @Column(name = "PERCENTUALE", precision = 9, scale = 4)
    private BigDecimal percentuale;
    @Column(name = "DATA_PREVISTA")
    private LocalDate dataPrevista;
    @Column(name = "DATA_SCADENZA")
    private LocalDate dataScadenza;
    @Column(name = "DESCRIZIONE_BASE", length = 2000)
    private String descrizioneBase;
    @Column(name = "DESCRIZIONE_SCRITTA", length = 2000)
    private String descrizioneScritta;
    @Column(name = "ANOMALIA", length = 2000)
    private String anomalia;
    @Column(name = "CLIENTE", length = 250)
    private String cliente;
    @Column(name = "STATO_ORDINE", length = 30)
    private String statoOrdine;
    @Column(name = "STATO", length = 30)
    private String stato;
    @Column(name = "IMPORTO_PREVISTO", precision = 18, scale = 2)
    private BigDecimal importoPrevisto;
    @Column(name = "IMPORTO_FATTURATO", precision = 18, scale = 2)
    private BigDecimal importoFatturato;
    @Column(name = "FATTURE", length = 2000)
    private String fatture;
    @Column(name = "CONTROLLATO_IL")
    private LocalDateTime controllatoIl;
}
