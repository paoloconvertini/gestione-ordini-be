package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SaldiMagazzinoPK implements Serializable {
    @Column
    private String marticolo;
    @Column
    private String var1;
    @Column
    private String var2;
    @Column
    private String var3;
    @Column
    private String var4;
    @Column
    private String var5;
    @Column
    private String mmagazzino;
    @Column
    private String qty;
}
