-- auto-generated definition
create table USER_ROLE
(
    user_id bigint not null,
    role_id bigint not null,
    primary key (role_id, user_id),
    constraint FK52veh3j2tgvkrogjw2i0tucac
        foreign key (role_id) references ROLE (id),
    constraint FKsn0101oegd6lsfnumn271ch3i
        foreign key (user_id) references USER (id)
);

