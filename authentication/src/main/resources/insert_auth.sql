INSERT INTO auth.USER (id,email, lastname, name, password, username)
VALUES (1, 'paolo.convertini@gmail.com',  'convertini', 'paolo', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'pconvertini');
INSERT INTO auth.USER (id, email, lastname, name, password, username, codVenditore)
VALUES (2,'damiano@calolenocifrancesco.it',  'calolenoci', 'damiano', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'dcalolenoci', '11');
INSERT INTO auth.USER (id, email, lastname, name, password, username)
VALUES (3, '', 'amministrazione', 'franco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'franco');
INSERT INTO auth.USER (id, email, lastname, name, password, username)
VALUES (4, '', 'magazzino', 'francesco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'francesco');
INSERT INTO auth.USER (id, email, lastname, name, password, username, codVenditore)
VALUES (5, 'piero@calolenocifrancesco.it', 'calolenoci', 'piero', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'pcalolenoci', '01');
INSERT INTO auth.USER (id,  email, lastname, name, password, username)
VALUES (6, '', 'logistica', 'daniele', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'daniele');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (7, 'piero@calolenocifrancesco.it', 'scialpi', 'lorenzo', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'lscialpi', '03');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (8, 'antonianna@calolenocifrancesco.it', 'fumarola', 'antonianna', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'afumarola', '08');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (9, 'roccocalolenoci@gmail.com', 'calolenoci', 'rocco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'rcalolenoci', '10');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (10, 'calolenoci.fra@gmail.com', 'devincienti', 'francesco', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'fdevincienti', '13');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (11, 'greece@calolenocifrancesco.it', 'semeraro', 'angela', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'asemeraro', '15');
INSERT INTO auth.USER (id,  email, lastname, name, password, username, codVenditore)
VALUES (12, 'info@calolenocifrancesco.it', '', 'mariagrazia', 'W54q1QK2gVP+cG6Hc4P4kQ==', 'mariagrazia', '16');





INSERT INTO auth.ROLE (id, name) VALUES (1, 'Admin');
INSERT INTO auth.ROLE (id, name) VALUES (2, 'User');
INSERT INTO auth.ROLE (id, name) VALUES (3, 'Magazziniere');
INSERT INTO auth.ROLE (id, name) VALUES (4, 'Venditore');
INSERT INTO auth.ROLE (id, name) VALUES (5, 'Amministrativo');
INSERT INTO auth.ROLE (id, name) VALUES (6, 'Logistica');

INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (1, 1);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (2, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (7, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (8, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (9, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (10, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (11, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (12, 4);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (3, 5);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (4, 3);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (5, 1);
INSERT INTO auth.USER_ROLE (user_id, role_id) VALUES (6, 6);
INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (5, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (7, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (8, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (9, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (2, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (10, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (11, 2);

INSERT INTO auth.USER_ROLE (user_id, role_id)
VALUES (12, 2);

INSERT INTO auth.USER_ROLE (role_id, user_id)
VALUES (4, 5);