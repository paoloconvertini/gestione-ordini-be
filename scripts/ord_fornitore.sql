CREATE TABLE ORDFOR2(
	ANNOOAF int NOT NULL,
	SERIEOAF varchar(3) NOT NULL,
	PROGRESSIVOOAF int NOT NULL,
	RIGO int NOT NULL,
	PROGRGENERALE int NULL,
	TIPORIGOOAF varchar(2) NULL,
	OARTICOLO varchar(13) NULL,
	SINONIMO1 int NULL,
	VARIANTE1 varchar(3) NULL,
	VARIANTE2 varchar(3) NULL,
	VARIANTE3 varchar(3) NULL,
	VARIANTE4 varchar(3) NULL,
	VARIANTE5 varchar(3) NULL,
	ODESCRARTICOLO varchar(50) NULL,
	CODICEEAN varchar(60) NULL,
	OSALDOACCONTO varchar(1) NULL,
	OQUANTITA float NULL,
	OQUANTITAV float NULL,
	OQUANTITA2 float NULL,
	OPREZZO float NULL,
	OVALORE float NULL,
	OUNITAMISURA varchar(2) NULL,
	OCOEFFICIENTE float NULL,
	OCODICEIVA varchar(3) NULL,
	FSCONTOARTICOLO float NULL,
	SCONTOF1 float NULL,
	SCONTOF2 float NULL,
	FSCONTOP float NULL,
	PREZZOEXTRA float NULL,
	OMAGAZZ varchar(3) NULL,
	DATARICHCONSEG datetime NULL,
	DATACONFCONSEG datetime NULL,
	OLOTTOMAGF varchar(10) NULL,
	OCOLLI int NULL,
	OPALLET float NULL,
	OCOMMESSA varchar(10) NULL,
	OCENTROCOSTOR varchar(10) NULL,
	OVOCESPESA varchar(10) NULL,
	IMPPROVVFISSO float NULL,
	OPROVVARTICOLO float NULL,
	OPROVVFORNITORE float NULL,
	QTYUSER1 float NULL,
	QTYUSER2 float NULL,
	QTYUSER3 float NULL,
	QTYUSER4 float NULL,
	QTYUSER5 float NULL,
	CAMPOUSER1 varchar(25) NULL,
	CAMPOUSER2 varchar(25) NULL,
	CAMPOUSER3 varchar(25) NULL,
	CAMPOUSER4 varchar(25) NULL,
	CAMPOUSER5 varchar(25) NULL,
	DATAUSER1 datetime NULL,
	DATAUSER2 datetime NULL,
	DATAUSER3 datetime NULL,
	DATAUSER4 datetime NULL,
	DATAUSER5 datetime NULL,
	PROVENIENZA varchar(1) NULL,
	PID int NULL,
	PID_PRIMANOTA int NULL,
	NOTEORDFOR2 text NULL,
	USERNAME varchar(20) NULL,
	DATAMODIFICA datetime NULL,
	SYS_CREATEDATE datetime NULL,
	SYS_CREATEUSER varchar(20) NULL,
	SYS_UPDATEDATE datetime NULL,
	SYS_UPDATEUSER varchar(20) NULL);
 ALTER TABLE ORDFOR2 ADD CONSTRAINT ORDFOR200 PRIMARY KEY CLUSTERED
(
	ANNOOAF ASC,
	SERIEOAF ASC,
	PROGRESSIVOOAF ASC,
	RIGO ASC
)

