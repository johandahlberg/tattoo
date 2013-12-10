# --- First database schema
 
# --- !Ups

CREATE TABLE projects (    
    name varchar(255) NOT NULL PRIMARY KEY,
    status varchar(255)
);

CREATE TABLE piperprojects (
    name varchar(255) NOT NULL PRIMARY KEY,
    logpath text,
    FOREIGN KEY (name) REFERENCES projects(name)
);

insert into projects(name, status) values ( 'exome_test', 'ongoing' );
insert into projects(name, status) values ( 'dd', 'ongoing' );

insert into piperprojects(name, logpath) values ( 'exome_test', '/proj/a2009002/private/nobackup/FU-Exom/piper_test/piper/pipeline_output/logs/exome.log' );
insert into piperprojects(name, logpath) values ( 'dd', '/some/fake/log/path' );
  
# --- !Downs

DROP TABLE IF EXISTS piperprojects; 
DROP TABLE IF EXISTS projects;
