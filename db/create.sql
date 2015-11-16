#CREATE DATABASE highway;
USE highway;

CREATE TABLE IF NOT EXISTS user_data(
	tmsi      VARCHAR(4000),
  	timestamp DATETIME,
  	lac       VARCHAR(8),
  	cellid    INT,
  	eventid   VARCHAR(2),
  	id	INT
);

CREATE TABLE IF NOT EXISTS cell_station(
	serialnumber	INT,
	cellid		INT,
	lacid		INT,
	nettype	INT,
	longitude	DOUBLE,
	latitude	DOUBLE
);

CREATE TABLE IF NOT EXISTS road_node(
	id		INT,
	road_node_code		INT,
	road_node_type		INT,
	road_node_name		VARCHAR(100),
	road_line_id	INT,
	road_node_km	DOUBLE,
	road_direction	INT,
	gis_y	DOUBLE,
	dep_code	INT,
	remark	INT,
	sde_id		VARCHAR(100),
	gis_x	DOUBLE
);

CREATE TABLE IF NOT EXISTS section(
	id		INT,
	section_code		VARCHAR(10),
	section_name		VARCHAR(30),
	road_direction		INT,
	start_road_node	INT,
	end_road_node	INT,
	design_speed	INT,
	travel_time	INT,
	effect_factor	INT,
	design_flow	INT,
	correspond_section	INT,
	sde_id		VARCHAR(100),
	section_type	INT,
	length		DOUBLE
);

CREATE TABLE IF NOT EXISTS section_speeds(
	id INT,
	name VARCHAR(100),
	time DATETIME,
	direction INT,
	max_speed INT,
	min_speed INT,
	avg_speed INT,
	num INT
);

CREATE TABLE IF NOT EXISTS station_speeds(
	id INT,
	startStation VARCHAR(100),
	time DATETIME,
	direction INT,
	max_speed INT,
	min_speed INT,
	avg_speed INT,
	fileter_speed INT,
	num INT
);

CREATE TABLE IF NOT EXISTS gps_data(
	altitude INT,
	gisX DOUBLE,
	gisY DOUBLE,
	goAndOut INT,
	mileage INT,
	speed INT,
	iMei VARCHAR(20),
	locateTime DATETIME,
	plateNumber VARCHAR(20),
	recordTime DATETIME,
	status VARCHAR(5),
	tranStatus VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS gps_data2(
	altitude INT,
	gisX DOUBLE,
	gisY DOUBLE,
	goAndOut INT,
	mileage INT,
	speed INT,
	iMei VARCHAR(20),
	locateTime DATETIME,
	plateNumber VARCHAR(20),
	poeDesc VARCHAR(200),
	recordTime DATETIME,
	status VARCHAR(5),
	tranStatus VARCHAR(5)
);





