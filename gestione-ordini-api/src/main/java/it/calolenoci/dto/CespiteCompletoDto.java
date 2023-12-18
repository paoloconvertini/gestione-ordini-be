package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteCompletoDto {

    CespiteSommaDto cespiteSommaDto;

    List<CategoriaCespitiDto> categoriaCespitiDtoList;
}
