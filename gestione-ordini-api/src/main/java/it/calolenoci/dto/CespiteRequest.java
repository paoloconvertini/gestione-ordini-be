package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CespiteRequest implements Serializable {
    private String id;
    private String tipoCespite;
    private Double perc;
}
