create table REGISTROAZIONI
(
    id          int          not null,
    anno        int          null,
    progressivo int          null,
    serie       varchar(3)   null,
    rigo        int          null,
    user        varchar(100) null,
    data        datetime     not null,
    azione      varchar(30)  not null,
    constraint REGISTROAZIONI_pk
        primary key (id)
);

create unique index REGISTROAZIONI_id_uindex
    on REGISTROAZIONI (id);

