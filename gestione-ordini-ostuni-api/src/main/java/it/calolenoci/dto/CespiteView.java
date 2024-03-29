package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CespiteView implements Serializable {

    private CespiteSommaDto cespiteSommaDto = new CespiteSommaDto();

    private List<CespiteDto> cespiteList = new ArrayList<>();
}
