delete from users_roles;
delete from users;
delete from roles;

insert into users (id,name,username,email,password) values (1,'Pepe', 'pepeillo', 'pepeillo@gmail.com','123456789');
insert into users (id, name, username, email, password) values (2, 'Rudd', 'Rudd_User', 'rmacmichael0@theglobeandmail.com', '31658306');
insert into users (id, name, username, email, password) values (3, 'Nadya', 'Nadya_User', 'ndivers1@reuters.com', '861201902');
insert into users (id, name, username, email, password) values (4, 'Alla', 'Alla_User', 'adallman2@e-recht24.de', '299326054');
insert into users (id, name, username, email, password) values (5, 'Cliff', 'Cliff_User', 'cmaryan3@google.pl', '06821235');
insert into users (id, name, username, email, password) values (6, 'Nanine', 'Nanine_User', 'nstangel4@etsy.com', '20511529');

insert into roles(id, name) values(1, 'ROLE_USER');
insert into roles(id, name) values(2, 'ROLE_ADMIN');

INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (5, 2);