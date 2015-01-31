# Oskari-Routing

Oskari-Routing provides in-application route calculation on Oskari platform. Oskari-Routing is submit entry to [Oskari Challenge 2015](http://oskari.org/challenge).

Oskari-Routing is a collection of enhancements both in the backend and in the front-end of Oskari. See [Oskari-Routing front end](http://todo).

## Table of Contents

TODO

## Demo Setup

In order to get Oskari-Routing to work some setup is required.

First setup Oskari server as instructed in [Oskari.org documentation](http://oskari.org/documentation/backend/server-embedded-developer).

### 1. Setup [pgRouting](http://pgrouting.org/) in the database

Even though Oskari-Routing implements proprietary A\* Algorithm it uses pgRouting to construct graph representation
from given road link data.

Oskari-Routing also provides support for pgRouting A\* Algorithm that can be used on side with the proprietary algorithm.

1. Install pgRouting from http://pgrouting.org/. Tested on pgRouting 2.0.0

2. Create pgRouting extension in oskari database by running the following as a db user that has superuser rights:
```
\c oskaridb
create extension if not exists pgrouting;
```

### 2. Acquire road link data that can be used for routing

As an example we are going to use road link data of downtown Helsinki for route calculation.
Data is fetched as a shape file from [National Land Survey of Finland](http://www.maanmittauslaitos.fi/en) database.

1. At the root folder run the script to fetch road links from NLS:
`./fetch-roadlinks-hki.sh`
This will create `roadlinks-hki.shp.zip` zipped shape file that contains road links of downtown Helsinki area.

2. Unzip the zipped shape file to a new directory:
```
mkdir roadlinks-hki
mv roadlinks-hki.shp.zip roadlinks-hki
cd roadlinks-hki
unzip roadlinks-hki.shp.zip
```

### 3. Import road link data to Oskari database

1. Create SQL script from shape file by running following in the directory to which road link shape file was extracted:  
`shp2pgsql -s 3067 -g geom -I -S -W LATIN1 tieviiva.shp public.hkiroads > hkiroads.sql`  
This will output `hkiroads.sql` - file to the directory in which the command is executed.  
The parameters depend on the road link data in question. These parameters are tailored for the data acquired in the
previous step and are explained below:  
    * -s 3067 -> Use specified projection identified by srid (ETRS89 / ETRS-TM35FIN)
    * -g geom -> Write road link geometry to `geom` column
    * -I -> Create spatial index on the geometry
    * -S -> Generate simple geometries (LineString instead of MultiLineString)
    * -W LATIN1 -> Use correct character encoding  

2. Import road links from `hkiroads.sql`-file to PostgreSQL as oskari user:
`psql -d oskaridb -U oskari -f hkiroads.sql -W`  
Here its assumed that Oskari uses db by role `oskari`    
In case of error: **ERROR: operator class "gist_geometry_ops" does not exist for access method "gist"**
Import `legacy_gist.sql` to PostgreSQL and import `hkiroads.sql`-file again.

### 4. Setup Oskari application

1. Make sure that the oskari user is the owner of the schema in which `hkiroads` table exists.
Without this pgRouting setup functions won't work and graph cannot be built.  
In our case run the following as the schema `public` owner:  
`alter schema public owner to oskari;`

2. Create functions required for Oskari-Routing. This step has to be completed manually since Oskari sql parser does not support definition of functions.  
Run the following in root directory:  
`psql -d oskaridb -U oskari -f content-resources/src/main/resources/sql/PostgreSQL/create-pgrouting-functions.sql`  
Replace `oskari` with the user role Oskari uses to connecto to database

3. Setup test application and create graph with pgRouting for route calculation.  
Run the following in the `content-resources` directory:  
`mvn compile exec:java -Doskari.dropdb=true -Doskari.setup=vector-layer -Ddb.username=oskari -Ddb.password=<oskaripasswd>`


## TODO:

* Rename setup script from vector-layer
* Allow definition of road link table name in configuration
* Allow routing from anywhere on edge (currently route calculation always starts and ends from/to the closest node)
* Support for remaining pgRouting algorithms
* Allow dynamic variables in route calculation (such as traffic conditions)

## Copyright and license

Copyright 2014 NLS under dual license MIT (included LICENSE-MIT.txt) and [EUPL](https://joinup.ec.europa.eu/software/page/eupl/licence-eupl)
(any language version applies, English version is included in LICENSE-EUPL.pdf).