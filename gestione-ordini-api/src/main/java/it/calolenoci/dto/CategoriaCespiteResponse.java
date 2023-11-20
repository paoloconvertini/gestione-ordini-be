package it.calolenoci.dto;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class CategoriaCespiteResponse implements Serializable {

    private String id;

    private String tipoCespite;
    
    private String codice;
    
    private String descrizione;

    private Double percAmmortamento;
    
    private Integer costoGruppo;
    
    private String costoConto;

    private Integer ammGruppo;

    private String ammConto;
    
    private Integer fondoGruppo;
    
    private String fondoConto;

    private Integer plusGruppo = 4311;

    private String plusConto = "000005";
    
    private Integer minusGruppo = 3311;
    
    private String minusConto = "000001";

    
}
