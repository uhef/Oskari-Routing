-- create extension pgrouting;

-- select pgr_createtopology('tieviiva2',0.001,the_geom:='geom2d',id:='gid');

-- select * from pgr_getTableName('tieviiva');

-- alter table tieviiva2 add source bigint;

-- alter table tieviiva2 add target bigint;

-- select * from tieviiva2 order by gid;

-- select ST_Force2D(geom) from tieviiva;

-- select * from geometry_columns;

-- select * from spatial_ref_sys where srtext like '%TM35FIN%';

-- alter table tieviiva2 add column geom2d geometry(LINESTRING, 3067);

-- select * from tieviiva2 where gid = 11;

-- update tieviiva2 t set geom2d = (select ST_Force2D(geom) from tieviiva2 where gid = t.gid);

-- select gid as id, cast(source as int4), cast(target as int4), cast(1 as float8) as cost from tieviiva2;

-- select pgr_astar('select gid as id, cast(source as int4), cast(target as int4), cast(1 as float8) as cost from tieviiva2', 1, 76, false, false);

-- SELECT PostGIS_full_version();