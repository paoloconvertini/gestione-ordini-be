package it.calolenoci.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ORDCLI")
public class Ordine extends PanacheEntityBase implements Serializable {

    @Column(length = 4)
    @Id
    public  Integer anno;

    @Column(length = 3)
    @Id
    public String serie;

    @Column
    @Id
    public Integer progressivo;


}
