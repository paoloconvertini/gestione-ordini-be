package it.calolenoci.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "USER")
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String lastname;

    @Column
    public Date dataNascita;

    @Column(nullable = false)
    @JsonIgnore
    public String password;

    @Column
    public String email;

    @Column(length = 3)
    public String codVenditore;

    @ManyToMany
    @JoinTable(
            name = "USER_ROLE",
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}),
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    public Set<Role> roles = new LinkedHashSet<>();

    public static User findByUsernameAndPassword(String username, String password){
        Map<String, Object> params = Parameters.with("username", username)
                .and("password", password)
                .map();

        return find("username =:username AND password =:password", params).firstResult();
    }

    public String getFullName(){
        return this.name + " " + this.lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

