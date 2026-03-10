package it.calolenoci.dto;

import lombok.Data;

import java.util.List;

@Data
public class VeicoloConsegneDto {

    private Integer idVeicolo;

    private String descrizione;

    private List<OrdineDTO> consegne;

    public Integer getIdVeicolo() {
        return idVeicolo;
    }

    public void setIdVeicolo(Integer idVeicolo) {
        this.idVeicolo = idVeicolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<OrdineDTO> getConsegne() {
        return consegne;
    }

    public void setConsegne(List<OrdineDTO> consegne) {
        this.consegne = consegne;
    }
}
