-- create extension pgrouting;

-- select pgr_createtopology('tieviiva2',0.001,the_geom:='geom2d',id:='gid');

-- select pgr_createtopology('hkiroads',0.001,the_geom:='geom2d',id:='gid');

-- select * from pgr_getTableName('hkiroads');

-- select strpos('public.hkiroads','.');

-- select current_schema;

-- SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'public';

select * from information_schema.tables;

-- select * from information_schema.schemata;

-- SELECT * FROM information_schema.schemata;

-- select * from pg_tables where schemaname = 'information_schema'

-- select * from information_schema.schemata;

-- alter function pgr_gettablename(text) owner to oskari;

-- select * from pgr_getTableName('tieviiva');

-- alter table tieviiva2 add column source bigint;

-- alter table tieviiva2 add column target bigint;

-- alter table tieviiva2 add column x1 double precision;
-- alter table tieviiva2 add column y1 double precision;
-- alter table tieviiva2 add column x2 double precision;
-- alter table tieviiva2 add column y2 double precision;

-- alter table tieviiva2 add column cost double precision;

-- select * from tieviiva2 order by gid;

-- select ST_Force2D(geom) from tieviiva;

-- select * from geometry_columns;

-- select * from spatial_ref_sys where srtext like '%TM35FIN%';

-- alter table tieviiva2 add column geom2d geometry(LINESTRING, 3067);

-- select * from tieviiva2 where gid = 11;

-- update tieviiva2 t set geom2d = (select ST_Force2D(geom) from tieviiva2 where gid = t.gid);

-- update tieviiva2 set x1 = st_x(st_startpoint(geom2d)),
--   y1 = st_y(st_startpoint(geom2d)),
--   x2 = st_x(st_endpoint(geom2d)),
--   y2 = st_y(st_endpoint(geom2d));

-- update tieviiva2 set cost = st_length(geom2d);

-- select gid as id, cast(source as int4), cast(target as int4), cost, x1, y1, x2, y2 from tieviiva2;

-- select pgr_astar('select gid as id, cast(source as int4), cast(target as int4), cost, x1, y1, x2, y2 from tieviiva2', 1, 76, false, false);

-- select gid, nimi_suomi from tieviiva2 where nimi_suomi like '%aarl%';

-- select gid, nimi_suomi from tieviiva2 where nimi_suomi like '%liel%';

-- select * from tieviiva2 where gid in (506, 940);

-- select pgr_astar('select gid as id, cast(source as int4), cast(target as int4), cost, x1, y1, x2, y2 from tieviiva2', 828, 14, false, false);

-- select * from hkiroads;