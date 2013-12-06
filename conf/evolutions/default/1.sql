# --- First database schema
 
# --- !Ups

CREATE SEQUENCE projects_id_seq;

CREATE TABLE projects (
    id long NOT NULL DEFAULT nextval('projects_id_seq'),
    name varchar(255),
    status varchar(255)
);

CREATE TABLE piperprojects (
    name varchar(255),
    logpath text
);

insert into projects(name, status) values ( 'aa', 'ongoing' );
insert into projects(name, status) values ( 'd', 'ongoing' );

insert into piperprojects(name, logpath) values ( 'aa', '/some/fake/log/path' );
 
 
# --- !Downs

DROP TABLE IF EXISTS piperprojects; 
DROP TABLE IF EXISTS projects;
DROP SEQUENCE projects_id_seq;