package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroCespitiDto implements Serializable {

    private CespiteSommaDto cespiteSommaDto = new CespiteSommaDto();

    private List<CategoriaCespitiDto> cespiteList = new ArrayList<>();

    private LocalDate data;

    public RegistroCespitiDto(CespiteSommaDto cespiteSommaDto, List<CategoriaCespitiDto> cespiteList) {
        this.cespiteSommaDto = cespiteSommaDto;
        this.cespiteList = cespiteList;
    }
}
