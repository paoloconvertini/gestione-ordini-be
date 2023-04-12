package it.calolenoci.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name="ROLE")
public class Role extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;

    @ManyToMany
    @JoinTable(name = "USER_ROLE",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> users = new LinkedHashSet<>();

    public static Role findByName(String name) {
        return find("name", name).firstResult();
    }
}