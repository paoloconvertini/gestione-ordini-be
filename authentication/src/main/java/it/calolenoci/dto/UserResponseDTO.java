package it.calolenoci.dto;/*
package it.paolo.convertini.dto;

import it.paolo.convertini.entity.User;

import java.util.Objects;

public class UserResponseDTO {
    private String name;
    private String lastname;
    private String username;

    public UserResponseDTO() {
    }

    public UserResponseDTO(String name, String lastname, String username) {
        this.name = name;
        this.lastname = lastname;
        this.username = username;
    }

    public UserResponseDTO(User u) {
        this.name = u.getName();
        this.lastname = u.getLastname();
        this.username = u.getUsername();
    }

    @Override
    public String toString() {
        return "UtenteResponseDTO{" +
                "name='" + name + '\'' +
                ", surname='" + lastname + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDTO that = (UserResponseDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(lastname, that.lastname) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lastname, username);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
*/
