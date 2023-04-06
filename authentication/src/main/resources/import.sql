INSERT INTO auth.USER (id, dataNascita, lastname, name, password, username)
VALUES (1, '2023-03-06 10:04:14.000000', 'convertini', 'paolo', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'pconvertini');
INSERT INTO auth.USER (id, dataNascita, lastname, name, password, username)
VALUES (2, '2023-03-06 10:04:14.000000', 'calolenoci', 'damiano', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'dcalolenoci');
INSERT INTO auth.USER (id, dataNascita, lastname, name, password, username)
VALUES (3, '2023-03-06 10:04:14.000000', 'amministrazione', 'franco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'franco');
INSERT INTO auth.USER (id, dataNascita, lastname, name, password, username)
VALUES (4, '2023-03-06 10:04:14.000000', 'magazzino', 'francesco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'francesco');
INSERT INTO auth.USER (id, dataNascita, lastname, name, password, username)
VALUES (5, '2023-03-06 10:04:14.000000', 'calolenoci', 'piero', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'pcalolenoci');





INSERT INTO auth.ROLE (id, name) VALUES (1, 'Admin');
INSERT INTO auth.ROLE (id, name) VALUES (2, 'User');
INSERT INTO auth.ROLE (id, name) VALUES (3, 'Magazziniere');
INSERT INTO auth.ROLE (id, name) VALUES (4, 'Venditore');
INSERT INTO auth.ROLE (id, name) VALUES (5, 'Amministrativo');

INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (1, 1);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (2, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (3, 5);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (4, 3);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (5, 1);