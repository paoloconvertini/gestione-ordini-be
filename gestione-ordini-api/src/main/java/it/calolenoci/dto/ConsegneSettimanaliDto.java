package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ConsegneSettimanaliDto implements Serializable {

    private List<GiornoConsegneDto> giorni;

}
