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

CREATE TABLE labprojects (
    name varchar(255) NOT NULL PRIMARY KEY,
    status varchar(255),
    FOREIGN KEY (name) REFERENCES projects(name)
);

CREATE TABLE samples (
    name varchar(255) NOT NULL PRIMARY KEY,
    project varchar(255),
    FOREIGN KEY (project) REFERENCES projects(name)
);

CREATE TABLE libraries (
    name varchar(255) NOT NULL PRIMARY KEY,
    status varchar(255),
    sample varchar(255),
    FOREIGN KEY (sample) REFERENCES samples(name)
);


insert into projects(name, status) values ( 'cc', 'inlab' );
insert into projects(name, status) values ( 'exome_test', 'analysisongoing' );
insert into projects(name, status) values ( 'dd', 'analysisongoing' );
insert into projects(name, status) values ( 'aa', 'analysisfinished' );
insert into projects(name, status) values ( 'bb', 'delivered' );

insert into piperprojects(name, logpath) values ( 'exome_test', '/proj/a2009002/private/nobackup/FU-Exom/piper_test/piper/pipeline_output/logs/exome.log' );
insert into piperprojects(name, logpath) values ( 'dd', '/some/fake/log/path' );

insert into labprojects(name, status) values ( 'cc', 'inlab' );

insert into samples(name, project) values ( 'sample1', 'cc');
insert into samples(name, project) values ( 'sample2', 'cc');

insert into libraries(name, status, sample) values ( 'lib1', 'sequenced', 'sample1');
insert into libraries(name, status, sample) values ( 'lib2', 'sequenced', 'sample1');
insert into libraries(name, status, sample) values ( 'lib3', 'sequenced', 'sample2');
  
# --- !Downs

DROP TABLE IF EXISTS piperprojects; 
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS labprojects;
DROP TABLE IF EXISTS samples;
DROP TABLE IF EXISTS libraries;
