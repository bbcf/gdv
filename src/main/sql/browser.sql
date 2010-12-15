-- USER AND GROUPS
create table users (
       id serial not null primary key,
       mail varchar(255) unique not null,
       name varchar(255),
       firstname varchar(255),
       title varchar(255),
       phone varchar(255),
       office varchar(255),
       type varchar(255)
);

create table admin (
       id serial not null references users(id)
);

create table groups (
       id serial not null primary key,
       mail varchar(255) not null,
	   name varchar(255) not null
);

create table userToGroup (
	group_id serial not null references groups(id) on delete cascade,
	user_id serial not null references users(id) on delete cascade
);



--SEQUENCES
create table sequences (
	id serial not null primary key,
	jbrowse_id serial not null,
	type varchar(255) not null
);
 create table species (
 	name varchar(255) not null
 
 );

--ANNOTATIONS AND TRACKS
-- userInput : the file from user input
create table userInput (
       id serial not null primary key,
       file varchar(255) not null unique
);

create table userToInput (
       user_id serial not null references users(id) on delete cascade,
       input_id serial not null references userInput(id) on delete cascade,
       the_date date not null
);

create table sequenceToUserInput (
       seq_id serial not null references sequences(id) on delete cascade,
       userInput_id serial not null references userInput(id) on delete cascade
);


-- tracks
create table tracks (
       id serial not null primary key,	
       track_name varchar(255) not null,
       paramaters text not null,
       filetype varchar(255) not null,
       always boolean not null,
       status varchar(255) not null
);

create table userToTrack (
        user_id serial not null references users(id) on delete cascade,
       	track_id serial not null references tracks(id) on delete cascade
);

create table adminTrack (
       track_id serial not null references tracks(id) on delete cascade,
       seq_id  serial not null references sequences(id) on delete cascade
);

create table fileToTrack (
       file_name varchar(255) not null references userInput(file) on delete cascade,
       track_id serial not null references tracks(id) on delete cascade
);

--PROJECTS
create table projects (
       id serial not null primary key,
       seq_id serial not null references sequences(id),	
       description varchar(255) not null
);

create table projectToTrack (
       project_id serial not null references projects(id) on delete cascade,
       track_id serial not null references tracks(id) on delete cascade,
       primary key (project_id,track_id)
);
create table userToProject (
       	user_id serial not null references users(id) on delete cascade,
	project_id serial not null references projects(id) on delete cascade,
	primary key (user_id,project_id)
);
