CREATE TABLE ORDCLI2(
	ANNO int NOT NULL,
	SERIE varchar(3) NOT NULL,
	PROGRESSIVO int NOT NULL,
	RIGO int NOT NULL,
	PROGRGENERALE int NULL,
	TIPORIGO varchar(2) NULL,
	FARTICOLO varchar(13) NULL,
	VARIANTE1 varchar(3) NULL,
	VARIANTE2 varchar(3) NULL,
	VARIANTE3 varchar(3) NULL,
	VARIANTE4 varchar(3) NULL,
	VARIANTE5 varchar(3) NULL,
	CODARTFORNITORE varchar(25) NULL,
	FDESCRARTICOLO varchar(50) NULL,
	CODICEEAN varchar(60) NULL,
	DATACONFCONSEGNA datetime NULL,
	DATARICHCONSEGNA datetime NULL,
	QUANTITA float NULL,
	QUANTITAV float NULL,
	QUANTITA2 float NULL,
	QTAOMAGGIO float NULL,
	PREZZO float NULL,
	FUNITAMISURA varchar(2) NULL,
	FCOEFFICIENTE float NULL,
	COEFPREZZO float NULL,
	SCONTOARTICOLO float NULL,
	SCONTOC1 float NULL,
	SCONTOC2 float NULL,
	SCONTOP float NULL,
	PREZZOEXTRA float NULL,
	MAGAZZ varchar(3) NULL,
	LOTTOMAGF varchar(10) NULL,
	FCODICEIVA varchar(3) NULL,
	FCENTROCOSTOR varchar(10) NULL,
	IMPPROVVFISSO float NULL,
	FPROVVARTICOLO float NULL,
	FPROVVCLIENTE float NULL,
	FGRUPPORICAVO int NULL,
	FCONTORICAVO varchar(6) NULL,
	SALDOACCONTO varchar(1) NULL,
	FCOLLI int NULL,
	FCOMMESSA varchar(10) NULL,
	FPALLET float NULL,
	PALLET varchar(3) NULL,
	QTYUSER1 float NULL,
	QTYUSER2 float NULL,
	QTYUSER3 float NULL,
	QTYUSER4 float NULL,
	QTYUSER5 float NULL,
	QTYUSER6 float NULL,
	QTYUSER7 float NULL,
	QTYUSER6I int NULL,
	QTYUSER7I int NULL,
	QTYUSER8I int NULL,
	QTYUSER9I int NULL,
	QTYUSER10I int NULL,
	DESCRUSER1 varchar(50) NULL,
	DESCRUSER2 varchar(50) NULL,
	DESCRUSER3 varchar(50) NULL,
	DESCRUSER4 varchar(50) NULL,
	DESCRUSER5 varchar(50) NULL,
	DESCRUSER6 varchar(50) NULL,
	DESCRUSER7 varchar(50) NULL,
	DATAUSER1 datetime NULL,
	DATAUSER2 datetime NULL,
	DATAUSER3 datetime NULL,
	DATAUSER4 datetime NULL,
	DATAUSER5 datetime NULL,
	DATAUSER6 datetime NULL,
	DATAUSER7 datetime NULL,
	FPROVENIENZA varchar(3) NULL,
	FPID int NULL,
	CONTROMARCA varchar(32) NULL,
	NOTEORDCLI2 text NULL,
	STATO varchar(1) NULL,
	DATAMODIFICA datetime NULL,
	USERNAME varchar(20) NULL,
	SYS_CREATEDATE datetime NULL,
	SYS_CREATEUSER varchar(20) NULL,
	SYS_UPDATEDATE datetime NULL,
	SYS_UPDATEUSER varchar(20) NULL);

ALTER TABLE ORDCLI2 ADD CONSTRAINT ORDCLI200 PRIMARY KEY CLUSTERED (
	ANNO ASC,
	SERIE ASC,
	PROGRESSIVO ASC,
	RIGO ASC
);

