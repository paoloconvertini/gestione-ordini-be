package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrdineResponseDto implements Serializable {


    private Map<String, Double> importoRiservatiMap = new HashMap<>();

    private List<OrdineDTO> ordineDTOList;
}
