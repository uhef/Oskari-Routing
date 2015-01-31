# Oskari-Routing

Oskari-Routing provides in-application route calculation on Oskari platform. Oskari-Routing is submit entry to [Oskari Challenge 2015](http://oskari.org/challenge).

Oskari-Routing is a collection of enhancements both in the backend and in the front-end of Oskari. See [Oskari-Routing front end](http://todo).

## Table of Contents

TODO

## Demo Setup

In order to get Oskari-Routing to work some setup is required.

First setup Oskari server as instructed in [Oskari.org documentation](http://oskari.org/documentation/backend/server-embedded-developer).

### Setup [pgRouting](http://pgrouting.org/) in the database

Even though Oskari-Routing implements proprietary A\* Algorithm it uses pgRouting to construct graph representation
from given road link data.

Oskari-Routing also provides support for pgRouting A\* Algorithm that can be used on-side with the proprietary algorithm.

1. Install pgRouting from http://pgrouting.org/. Tested on pgRouting 2.0.0

2. Create pgRouting extension in oskari database by running the following as a db user that has superuser rights:
```
\c oskaridb
create extension if not exists pgrouting;
```

### Acquire road link data that can be used for routing

As an example we are going to use road link data of downtown Helsinki for route calculation.
Data is fetched as a shape file from [National Land Survey of Finland](http://www.maanmittauslaitos.fi/en) database.

1. At the root folder run the script to fetch road links from NLS:
`./fetch-roadlinks-hki.sh`
This will create `roadlinks-hki.shp.zip` zipped shape file that contains road links of downtown Helsinki area.

2. Unzip the zipped shape file to a new directory:
`mkdir roadlinks-hki`
`mv roadlinks-hki.shp.zip roadlinks-hki`
`cd roadlinks-hki`
`unzip roadlinks-hki.shp.zip`

## Copyright and license

Copyright 2014 NLS under dual license MIT (included LICENSE-MIT.txt) and [EUPL](https://joinup.ec.europa.eu/software/page/eupl/licence-eupl)
(any language version applies, English version is included in LICENSE-EUPL.pdf).
