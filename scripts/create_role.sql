-- auto-generated definition
create table ROLE
(
    id   bigint       not null
        primary key,
    name varchar(255) not null,
    constraint UK_9glod3qre7ighyp4ci4t6fcoy
        unique (name)
);