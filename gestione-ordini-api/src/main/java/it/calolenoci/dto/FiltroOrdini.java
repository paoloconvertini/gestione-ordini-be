package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FiltroOrdini implements Serializable {

    private String codVenditore;

    private String status;

    private List<String> stati;

    private Boolean prontoConsegna = Boolean.FALSE;

}
