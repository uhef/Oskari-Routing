#!/bin/sh
curl "http://avoindata.maanmittauslaitos.fi/geoserver/wfs?request=GetFeature&typeNames=mtk:tieviiva&outputFormat=SHAPE-ZIP&bbox=383419.456,6670369.695,387390.459,6673843.678" > roadlinks-hki.shp.zip

