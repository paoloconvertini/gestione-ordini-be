-- auto-generated definition
create table USER
(
    id           bigint       not null
        primary key,
    codVenditore varchar(3)   null,
    dataNascita  datetime(6)  null,
    email        varchar(255) null,
    lastname     varchar(255) not null,
    name         varchar(255) not null,
    password     varchar(255) not null,
    username     varchar(255) not null,
    constraint UK_pc8a3iwr0i5534lmd6b1jdg1w
        unique (username)
);

