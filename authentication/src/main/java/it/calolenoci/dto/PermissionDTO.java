package it.calolenoci.dto;

import java.util.List;

public class PermissionDTO {
    public Long id;
    public String name;
    public String description;
    public List<RoleDto> roles;
}
