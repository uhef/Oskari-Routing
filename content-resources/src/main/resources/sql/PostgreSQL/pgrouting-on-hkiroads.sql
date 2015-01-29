alter table hkiroads
  drop column if exists geom2d,
  drop column if exists source,
  drop column if exists target,
  drop column if exists x1,
  drop column if exists y1,
  drop column if exists x2,
  drop column if exists y2,
  drop column if exists cost;

alter table hkiroads
  add column geom2d geometry(LINESTRING, 3067),
  add column source bigint,
  add column target bigint,
  add column x1 double precision,
  add column y1 double precision,
  add column x2 double precision,
  add column y2 double precision,
  add column cost double precision;

update hkiroads set geom2d = st_force2D(geom);

update hkiroads set x1 = st_x(st_startpoint(geom2d)),
  y1 = st_y(st_startpoint(geom2d)),
  x2 = st_x(st_endpoint(geom2d)),
  y2 = st_y(st_endpoint(geom2d));

update hkiroads set cost = st_length(geom2d);

-- Create graph (i.e. source and target nodes of each edge);
select pgr_createtopology('hkiroads',0.001,the_geom:='geom2d',id:='gid');

