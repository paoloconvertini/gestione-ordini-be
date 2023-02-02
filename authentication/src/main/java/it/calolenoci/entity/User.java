package it.calolenoci.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "USER")
@NoArgsConstructor
@AllArgsConstructor
public class User extends PanacheEntity implements Serializable {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String lastname;

    @Column(nullable = false)
    public Date dataNascita;

    @Column(nullable = false)
    public String password;

    @ManyToMany
    @JoinTable(
            name = "USER_ROLE",
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}),
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    public Collection<Role> roles = new ArrayList<>();

    public static User findByUsernameAndPassword(String username, String password){
        Map<String, Object> params = Parameters.with("username", username)
                .and("password", password)
                .map();

        return find("username =:username AND password =:password", params).firstResult();
    }

}

