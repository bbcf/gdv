CREATE TABLE users
(
"id" SERIAL NOT NULL,
"mail" VARCHAR(255) NOT NULL,
"name" VARCHAR(255),
"firstname" VARCHAR(255),
"title" VARCHAR(255),
"phone" VARCHAR(255),
"office" VARCHAR(255),
"type" VARCHAR(255) NOT NULL,
"the_date" DATE NOT NULL,
"key" VARCHAR(255),
PRIMARY KEY ("id")
);

CREATE TABLE groups
(
"id" SERIAL NOT NULL,
"owner" SERIAL NOT NULL,
"name" VARCHAR(255),
PRIMARY KEY ("id")
);

CREATE TABLE admin
(
"id" SERIAL NOT NULL
);

CREATE TABLE input
(
"id" SERIAL NOT NULL,
"md5" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE species
(
"id" SERIAL NOT NULL,
"name" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE sequences
(
"id" SERIAL NOT NULL,
"jbrowse_id" SERIAL NOT NULL,
"type" VARCHAR(255) NOT NULL,
"name" VARCHAR(255) NOT NULL,
"species_id" INTEGER NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE projects
(
"id" SERIAL NOT NULL,
"cur_seq_id" SERIAL NOT NULL,
"name" VARCHAR(255) NOT NULL,
"isPublic" boolean NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE publicProjects
(
"project_id" SERIAL NOT NULL,
"public_key" varchar(255) NOT NULL,
PRIMARY KEY ("project_id")
);
CREATE TABLE groupToProject
(
"group_id" SERIAL NOT NULL,
"project_id" SERIAL NOT NULL,
PRIMARY KEY ("group_id","project_id")
);

CREATE TABLE userToInput
(
"user_id" SERIAL NOT NULL,
"input_id" SERIAL NOT NULL,
"the_date" DATE NOT NULL
);

CREATE TABLE userToGroup
(
"user_mail" VARCHAR(255) NOT NULL,
"group_id" SERIAL NOT NULL,
PRIMARY KEY ("user_mail","group_id")
);

CREATE TABLE userToProject
(
"user_id" SERIAL NOT NULL,
"project_id" SERIAL NOT NULL,
PRIMARY KEY ("user_id","project_id")
);

CREATE TABLE tracks
(
"id" SERIAL NOT NULL,
"job_id" INTEGER UNIQUE NOT NULL,
"name" VARCHAR(255) NOT NULL,
"paramaters" TEXT NOT NULL,
"status" VARCHAR(255) NOT NULL,
"type" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE projectToTrack
(
"project_id" INTEGER NOT NULL,
"track_id" INTEGER NOT NULL,
PRIMARY KEY ("project_id","track_id")
);

CREATE TABLE inputToTrack
(
"input_id" SERIAL NOT NULL,
"track_id" SERIAL NOT NULL
);

CREATE TABLE admin_tracks
(
"track_id" INTEGER,
"sequence_id" INTEGER
);

CREATE TABLE gfeatminerjob
(
"id" SERIAL NOT NULL,
"project_id" INTEGER,
"status" INTEGER,
"result" VARCHAR(255)
);

CREATE TABLE statuses
(
"id" INTEGER NOT NULL,
"status" TEXT NOT NULL,
PRIMARY KEY ("id")
);


CREATE TYPE job_type AS ENUM ('new_selection','new_track','gfeatminer');
CREATE TYPE job_output AS ENUM ('reload', 'image');

CREATE TABLE jobs(
"id" SERIAL NOT NULL,
"status" INTEGER NOT NULL,
"project_id" INTEGER NOT NULL,
"data" TEXT NOT NULL,
"type" job_type NOT NULL,
"output" job_output NOT NULL,
PRIMARY KEY ("id")
);




CREATE TYPE style_height AS ENUM ('small', 'medium','big');
CREATE TYPE style_color as ENUM ('blue','green','red','yellow','pink','black','BlueViolet','Chocolate','Orange','CornflowerBlue','Crimson','Cyan','DarkOliveGreen','DarkOrchid','DarkSalmon','Gold','LawnGreen','Magenta','NavajoWhite','Orchid','SaddleBrown','SteelBlue','YellowGreen');

CREATE TABLE styles(
"id" SERIAL NOT NULL,
"height" style_height NOT NULL,
"color" style_color NOT NULL,
PRIMARY KEY ("id")
);


CREATE TABLE user_style(
"user_id" INTEGER NOT NULL,
"type_id" INTEGER NOT NULL,
"style_id" INTEGER NOT NULL,
PRIMARY KEY ("user_id","type_id")
);

CREATE TABLE types(
"id" SERIAL NOT NULL,
"name" TEXT NOT NULL,
PRIMARY KEY("id")
);



ALTER TABLE trackToStyle ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE trackToStyle ADD FOREIGN KEY ("track_id") REFERENCES "tracks" ("id") on delete cascade;

ALTER TABLE trackToStyle ADD FOREIGN KEY ("style_id") REFERENCES "styles" ("id") on delete cascade;

ALTER TABLE tracks ADD FOREIGN KEY ("job_id") REFERENCES "jobs" ("id") on delete cascade;

ALTER TABLE jobs ADD FOREIGN KEY ("status") REFERENCES "statuses" ("id") on delete cascade;

ALTER TABLE jobs ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE gfeatminerjob ADD FOREIGN KEY ("status") REFERENCES "statuses" ("id") on delete cascade;

ALTER TABLE gfeatminerjob ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE admin_tracks ADD FOREIGN KEY ("track_id") REFERENCES "tracks" ("id") on delete cascade;

ALTER TABLE publicProjects ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE admin_tracks ADD FOREIGN KEY ("sequence_id") REFERENCES "sequences" ("id") on delete cascade;

ALTER TABLE users ADD UNIQUE ("mail");

ALTER TABLE groups ADD FOREIGN KEY ("owner") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE admin ADD FOREIGN KEY ("id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE sequences ADD FOREIGN KEY ("species_id") REFERENCES "species" ("id") on delete cascade;

ALTER TABLE groupToProject ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("id") on delete cascade;

ALTER TABLE groupToProject ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE userToInput ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE userToInput ADD FOREIGN KEY ("input_id") REFERENCES "input" ("id") on delete cascade;

ALTER TABLE userToGroup ADD FOREIGN KEY ("user_mail") REFERENCES "users" ("mail") on delete cascade;

ALTER TABLE userToGroup ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("id") on delete cascade;

ALTER TABLE userToProject ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE userToProject ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE projectToTrack ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE projectToTrack ADD FOREIGN KEY ("track_id") REFERENCES "tracks" ("id") on delete cascade;

ALTER TABLE inputToTrack ADD FOREIGN KEY ("input_id") REFERENCES "input" ("id") on delete cascade;

ALTER TABLE inputToTrack ADD FOREIGN KEY ("track_id") REFERENCES "tracks" ("id") on delete cascade;

ALTER TABLE inputToTrack ADD UNIQUE ("input_id","track_id");




-- STATUSES
INSERT into statuses values(0,'ERROR');
INSERT into statuses values(1,'SUCCES');
INSERT into statuses values(2,'RUNNING');

-- ADMIN PROJECT
INSERT into projects values(-1,70,'admin',false);

-- DEFAULT STYLES
INSERT into sytles values(default,'small','blue');
INSERT into styles values(default,'small','blue');
INSERT into styles values(default,'small','green');
INSERT into styles values(default,'small','red');
INSERT into styles values(default,'small','yellow');
INSERT into styles values(default,'small','pink');
INSERT into styles values(default,'small','black');
INSERT into styles values(default,'small','BlueViolet');
INSERT into styles values(default,'small','Chocolate');
INSERT into styles values(default,'small','Orange');
INSERT into styles values(default,'small','CornflowerBlue');
INSERT into styles values(default,'small','Crimson');
INSERT into styles values(default,'small','Cyan');
INSERT into styles values(default,'small','DarkOliveGreen');
INSERT into styles values(default,'small','DarkOrchid');
INSERT into styles values(default,'small','DarkSalmon');
INSERT into styles values(default,'small','Gold');
INSERT into styles values(default,'small','LawnGreen');
INSERT into styles values(default,'small','Magenta');
INSERT into styles values(default,'small','NavajoWhite');
INSERT into styles values(default,'small','Orchid');
INSERT into styles values(default,'small','SaddleBrown');
INSERT into styles values(default,'small','SteelBlue');
INSERT into styles values(default,'small','YellowGreen');



INSERT into styles values(default,'medium','blue');
INSERT into styles values(default,'medium','green');
INSERT into styles values(default,'medium','red');
INSERT into styles values(default,'medium','yellow');
INSERT into styles values(default,'medium','pink');
INSERT into styles values(default,'medium','black');
INSERT into styles values(default,'medium','BlueViolet');
INSERT into styles values(default,'medium','Chocolate');
INSERT into styles values(default,'medium','Orange');
INSERT into styles values(default,'medium','CornflowerBlue');
INSERT into styles values(default,'medium','Crimson');
INSERT into styles values(default,'medium','Cyan');
INSERT into styles values(default,'medium','DarkOliveGreen');
INSERT into styles values(default,'medium','DarkOrchid');
INSERT into styles values(default,'medium','DarkSalmon');
INSERT into styles values(default,'medium','Gold');
INSERT into styles values(default,'medium','LawnGreen');
INSERT into styles values(default,'medium','Magenta');
INSERT into styles values(default,'medium','NavajoWhite');
INSERT into styles values(default,'medium','Orchid');
INSERT into styles values(default,'medium','SaddleBrown');
INSERT into styles values(default,'medium','SteelBlue');
INSERT into styles values(default,'medium','YellowGreen');





INSERT into styles values(default,'big','blue');
INSERT into styles values(default,'big','green');
INSERT into styles values(default,'big','red');
INSERT into styles values(default,'big','yellow');
INSERT into styles values(default,'big','pink');
INSERT into styles values(default,'big','black');
INSERT into styles values(default,'big','BlueViolet');
INSERT into styles values(default,'big','Chocolate');
INSERT into styles values(default,'big','Orange');
INSERT into styles values(default,'big','CornflowerBlue');
INSERT into styles values(default,'big','Crimson');
INSERT into styles values(default,'big','Cyan');
INSERT into styles values(default,'big','DarkOliveGreen');
INSERT into styles values(default,'big','DarkOrchid');
INSERT into styles values(default,'big','DarkSalmon');
INSERT into styles values(default,'big','Gold');
INSERT into styles values(default,'big','LawnGreen');
INSERT into styles values(default,'big','Magenta');
INSERT into styles values(default,'big','NavajoWhite');
INSERT into styles values(default,'big','Orchid');
INSERT into styles values(default,'big','SaddleBrown');
INSERT into styles values(default,'big','SteelBlue');
INSERT into styles values(default,'big','YellowGreen');
