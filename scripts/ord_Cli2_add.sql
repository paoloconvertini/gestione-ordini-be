ALTER TABLE ORDCLI2 ADD

    GE_STATUS varchar(50) NULL,
    GE_FLAG_RISERVATO       char(1) default 0,
    GE_FLAG_NON_DISPONIBILE char(1) default 0,
    GE_FLAG_ORDINATO char(1) default 0,
    GE_FLAG_CONSEGNATO char(1) default 0,
    GE_TONO varchar(20) NULL
;