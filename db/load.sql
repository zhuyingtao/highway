USE highway;

delete from section;
delete from road_node;
delete from cell_station;
delete from user_data;

load data local infile"data/����.txt" into table section character set utf8;
load data local infile"data/�ڵ�.txt" into table road_node character set utf8;
load data local infile"data/���ٻ�վ�б�.txt" into table cell_station;

load data local infile 'I:\\zyt\\Desktop\\highway_data\\tmp_no_res_user_hmh_6.txt' into table user_data fields terminated by ',';