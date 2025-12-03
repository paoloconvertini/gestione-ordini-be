CREATE TABLE PERMISSION (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description VARCHAR(255),
                            PRIMARY KEY (id)
);

CREATE TABLE ROLE_PERMISSION (
                                 role_id BIGINT NOT NULL,
                                 permission_id BIGINT NOT NULL,
                                 PRIMARY KEY (role_id, permission_id),
                                 CONSTRAINT fk_role_perm_role FOREIGN KEY(role_id) REFERENCES ROLE(id),
                                 CONSTRAINT fk_role_perm_perm FOREIGN KEY(permission_id) REFERENCES PERMISSION(id)
);
