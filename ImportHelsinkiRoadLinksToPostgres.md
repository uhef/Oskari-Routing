// TODO: How to acquire shape file?

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

3. Make sure oskari user is the owner of schema `public` in oskari database.
If not run following as the schema owner:
`alter schema public owner to oskari;`

 Without this pgrouting functions won't work and graph cannot be built.

