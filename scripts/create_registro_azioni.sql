create table GO_REGISTROAZIONI
(
    id          varchar(36)          not null,
    anno        int          null,
    progressivo int          null,
    serie       varchar(3)   null,
    rigo        int          null,
    username        varchar(100) null,
    createDate        datetime     not null,
    azione      varchar(30)  not null,
    quantita float null,
    tono varchar(20) null,
    qtaRiservata float null,
    qtaProntoConsegna float null,
    constraint GO_REGISTROAZIONI_pk
        primary key (id)
    );

create unique index GO_REGISTROAZIONI_id_uindex
    on GO_REGISTROAZIONI (id);