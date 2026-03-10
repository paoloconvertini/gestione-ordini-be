package it.calolenoci.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FasciaConsegneDto implements Serializable {

    private Character fascia; // M oppure P

    private List<VeicoloConsegneDto> veicoli;

    public Character getFascia() {
        return fascia;
    }

    public void setFascia(Character fascia) {
        this.fascia = fascia;
    }

    public List<VeicoloConsegneDto> getVeicoli() {
        return veicoli;
    }

    public void setVeicoli(List<VeicoloConsegneDto> veicoli) {
        this.veicoli = veicoli;
    }
}
