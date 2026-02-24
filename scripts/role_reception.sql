INSERT INTO ROLE (ID, NAME) VALUES (7,'Reception');
INSERT INTO PERMISSION (NAME) VALUES ('showroom.view');
INSERT INTO PERMISSION (NAME) VALUES ('showroom.edit');

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
         JOIN PERMISSION p ON p.NAME IN ('showroom.view', 'showroom.edit')
WHERE r.NAME = 'Reception';

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
SET t.description = 'consente l\'accesso alla lista delle visite'
WHERE t.id = 268;
