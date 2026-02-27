INSERT INTO PERMISSION (NAME) VALUES ('showroom.view');
INSERT INTO PERMISSION (NAME) VALUES ('showroom.edit');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME IN ('showroom.view', 'showroom.edit')
WHERE r.NAME = 'Admin';

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME IN ('showroom.view')
WHERE r.NAME = 'Venditore';

UPDATE auth.PERMISSION t
SET t.description = 'consente modifiche alla lista delle visite'
WHERE t.id = 269;

UPDATE auth.PERMISSION t
SET t.description = 'consente l''accesso alla lista delle visite'
WHERE t.id = 268;

INSERT INTO PERMISSION (NAME, DESCRIPTION)
VALUES ('motivi.view', 'consente gestione completa dei motivi showroom');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME = 'motivi.view'
WHERE r.NAME = 'Admin';

INSERT INTO ROLE (id, NAME) VALUES (7,'Reception_Ceglie');
INSERT INTO ROLE (id, NAME) VALUES (8, 'Reception_Ostuni');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME IN ('showroom.view', 'showroom.edit')
WHERE r.NAME = 'Reception_Ceglie';

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME IN ('showroom.view', 'showroom.edit')
WHERE r.NAME = 'Reception_Ostuni';

INSERT INTO PERMISSION (NAME, DESCRIPTION)
VALUES ('showroom.sede.filter', 'consente di filtrare le visite per sede');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME = 'showroom.sede.filter'
WHERE r.NAME = 'Admin';

INSERT INTO PERMISSION (NAME, DESCRIPTION)
VALUES ('logistica.view', 'Consente accesso al menu Logistica');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME = 'logistica.view'
WHERE r.NAME IN ('Admin','Logistica','Magazziniere');