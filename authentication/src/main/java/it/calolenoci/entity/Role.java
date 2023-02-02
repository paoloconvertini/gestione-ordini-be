package it.calolenoci.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name="ROLE")
public class Role extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    public Collection<User> users;

    public static Role findByName(String name) {
        return find("name", name).firstResult();
    }
}