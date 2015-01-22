INSERT INTO oskari_maplayer(type, name, groupId,
                            minscale, maxscale,
                            url, username, password, version, srs_name, locale)
  VALUES('vectorlayer', 'example_vectorlayer', (SELECT MAX(id) FROM oskari_layergroup),
         300000, 1,
         'http://oskari.org/vectorlayer/example','','','1.1.0', 'EPSG:3067','{fi:{name:"Vektoritaso", subtitle:""},sv:{name:"", subtitle:""},en:{name:"Vector layer", subtitle:""}}');

-- link to inspire theme;
INSERT INTO oskari_maplayer_themes(maplayerid,
                                   themeid)
  VALUES((SELECT MAX(id) FROM oskari_maplayer),
         (SELECT id FROM portti_inspiretheme WHERE locale LIKE '%Others%'));

-- add layer as resource for mapping permissions;
INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'vectorlayer+http://oskari.org/vectorlayer/example+example_vectorlayer');

-- give view_layer permission for the resource to guest role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'vectorlayer+http://oskari.org/vectorlayer/example+example_vectorlayer'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'Guest'));

-- give view_layer permission for the resource to user role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'vectorlayer+http://oskari.org/vectorlayer/example+example_vectorlayer'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'User'));

-- give view_layer permission for the resource to admin role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
((SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = 'vectorlayer+http://oskari.org/vectorlayer/example+example_vectorlayer'), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'Admin'));

