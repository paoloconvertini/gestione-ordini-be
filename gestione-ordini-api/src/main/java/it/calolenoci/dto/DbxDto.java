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
public class DbxDto implements Serializable {

   private List<String> codArtFornList = new ArrayList<>();
   private List<DbxSearchDTO> searchDTOS = new ArrayList<>();
}
