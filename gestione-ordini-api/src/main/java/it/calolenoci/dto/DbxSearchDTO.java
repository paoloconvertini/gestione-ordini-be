package it.calolenoci.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbxSearchDTO implements Serializable {

    private String fileName;

    private String path;
}
