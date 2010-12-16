--USER AND GROUP

CREATE TABLE "users"
(
"id" SERIAL NOT NULL,
"mail" VARCHAR(255) NOT NULL,
"name" VARCHAR(255),
"firstname" VARCHAR(255),
"title" VARCHAR(255),
"phone" VARCHAR(255),
"office" VARCHAR(255),
"type" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE "admin"
(
"id" SERIAL NOT NULL
);

CREATE TABLE "groups"
(
"id" SERIAL NOT NULL,
"owner" SERIAL NOT NULL,
"name" VARCHAR(255),
PRIMARY KEY ("id")
);

CREATE TABLE "userToGroup"
(
"user_id" SERIAL NOT NULL,
"group_id" SERIAL NOT NULL
);



--SEQUENCE
CREATE TABLE "sequences"
(
"id" SERIAL NOT NULL,
"jbrowse_id" SERIAL NOT NULL,
"type" VARCHAR(255) NOT NULL,
"name" VARCHAR(255) NOT NULL,
"species_id" INTEGER NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE "species"
(
"id" SERIAL NOT NULL,
"name" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);





--INPUTS
CREATE TABLE "tracks"
(
"id" SERIAL NOT NULL,
"name" VARCHAR(255) NOT NULL,
"paramaters" TEXT NOT NULL,
"status" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);



CREATE TABLE "input"
(
"id" SERIAL NOT NULL,
"md5" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE "inputToTrack"
(
"input_id" SERIAL NOT NULL,
"track_id" SERIAL NOT NULL
);

CREATE TABLE "userToInput"
(
"user_id" SERIAL NOT NULL,
"input_id" SERIAL NOT NULL,
"the_date" DATE NOT NULL,
"name" VARCHAR(255) NOT NULL
);



--PROJECTS
CREATE TABLE "projects"
(
"id" SERIAL NOT NULL,
"cur_seqid" SERIAL NOT NULL,
"name" VARCHAR(255) NOT NULL,
PRIMARY KEY ("id")
);

CREATE TABLE "userToProject"
(
"user_id" SERIAL NOT NULL,
"project_id" SERIAL NOT NULL,
PRIMARY KEY ("user_id","project_id")
);

CREATE TABLE "groupToProject"
(
"group_id" SERIAL NOT NULL,
"project_id" SERIAL NOT NULL,
PRIMARY KEY ("group_id","project_id")
);



--FOREIGN KEY


ALTER TABLE "admin" ADD FOREIGN KEY ("id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE "inputToTrack" ADD FOREIGN KEY ("input_id") REFERENCES "input" ("id") on delete cascade;

ALTER TABLE "inputToTrack" ADD FOREIGN KEY ("track_id") REFERENCES "tracks" ("id") on delete cascade;

ALTER TABLE "userToInput" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE "userToInput" ADD FOREIGN KEY ("input_id") REFERENCES "input" ("id") on delete cascade;

ALTER TABLE "groups" ADD FOREIGN KEY ("owner") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE "userToGroup" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE "userToGroup" ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("id") on delete cascade;

ALTER TABLE "sequences" ADD FOREIGN KEY ("species_id") REFERENCES "species" ("id") on delete cascade;

ALTER TABLE "projects" ADD FOREIGN KEY ("cur_seqid") REFERENCES "sequences" ("id") on delete cascade;

ALTER TABLE "userToProject" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") on delete cascade;

ALTER TABLE "userToProject" ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

ALTER TABLE "groupToProject" ADD FOREIGN KEY ("group_id") REFERENCES "groups" ("id") on delete cascade;

ALTER TABLE "groupToProject" ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("id") on delete cascade;

