INSERT INTO portti_bundle (name, startup)
       VALUES ('inapprouting','{}');

UPDATE portti_bundle set startup = '{
        "title" : "InAppRouting",
        "bundleinstancename" : "inapprouting",
        "fi" : "Hevonen",
        "sv" : "Häst",
        "en" : "Horse",
        "bundlename" : "inapprouting",
        "metadata" : {
            "Import-Bundle" : {
                "inapprouting" : {
                    "bundlePath" : "/Oskari/packages/routing/bundle/"
                }
            }
        }
    }' WHERE name = 'inapprouting';

