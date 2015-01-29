// TODO: How to acquire shape file?

Enable pgrouting on PostgreSQL database:
1. Install pgrouting: http://pgrouting.org/
2. Create extension in oskari database by running the following as a user that has superuser rights:
```
\c oskaridb
create extension if not exists pgrouting;
```

1. Create SQL script from shape file:
`shp2pgsql -s 3067 -g geom -I -S -W LATIN1 tieviiva.shp public.hkiroads > hkiroads.sql`

-s 3067 -> Use correct projection (ETRS89 / ETRS-TM35FIN)
-g geom -> Write road link geometry to `geom` column
-I -> Create spatial index on the geometry
-S -> Generate simple geometries (LineString instead of MultiLineString)
-W LATIN1 -> Use correct character encoding

Outputs hkiroads.sql

2. Import road links to PostgreSQL as oskari user:
`psql -d oskaridb -U oskari -f hkiroads.sql -W`

In case of error:
    ERROR: operator class "gist_geometry_ops" does not exist for access method "gist"
Import `legacy_gist.sql` to PostgreSQL. Something along these lines:
`psql -d oskaridb -f /usr/share/postgresql/9.1/contrib/postgis-2.1/legacy_gist.sql`
and reimport hkiroads.sql.

3. Make sure that the user used to run content-resources scripts is the owner of the schema in which `hkiroads` table exists.
Without this pgrouting setup functions won't work and graph cannot be built.

To find the owner of the schema in which `hkiroads` table exists run the following:
`select schema_owner from information_schema.schemata where schema_name = (select table_schema from information_schema.tables where table_name = 'hkiroads');`
If this is someone else than the user used to run scripts in content-resources you need to change the owner of the schema.

To find the schema in which `hkiroads` table exists run the following:
`select table_schema from information_schema.tables where table_name = 'hkiroads';`

Now if content-resources uses `oskari` user to run scripts and `hkiroads` table is defined in schema `public`
run the following as the schema owner:
`alter schema public owner to oskari;`
Replace `public` and `oskari` in the above to match your system.

4. Create functions required for inapprouting. This step has to be completed manually since Oskari sql parser does not support definition of functions:
`psql -d oskaridb -U oskari -f content-resources/src/main/resources/sql/PostgreSQL/create-pgrouting-functions.sql`
Replace `oskari` with the user role content-resources uses in database

TODO (in no particular order):
* Improve UI
* Allow routing from anywhere on an edge
* Introduce shortest route on backend for dynamic costs

