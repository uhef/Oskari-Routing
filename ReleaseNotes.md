# Release Notes

## 1.17.2

### GetFeatureInfoHandler

Styles will now longer be sent with value "null", but an empty string

### Transport

MapClick will now send an empty list as response when done so client knows that any data gotten for WMSlayers can be shown

## 1.17.1

### ZoomParamHandler

Now uses parameter correctly again (not trying to get a property with param value).

## 1.17

### service-permission

Added getGuestUser() method for UserService. Implementations should override it and return a Guest user with a Guest role so permission mappings can be done correctly.

### service-map

Added ibatis caching for Inspire-themes, views, wfs-layers and backendstatus operations.

### servlet-map

Added oskariui style classes to index.jsp to fix layout. 

Removed Guest user role hardcoding and now uses UserService.getGuestUser() to create a guest user.

### Massive maplayer refactoring

Maplayer DB structure and JSON formatting has been simplified so all layers are now located in oskari_maplayer database table and all JSON formatting should be done with
fi.mml.map.mapwindow.util.OskariLayerWorker instead of former MapLayerWorker. All layers should now be referenced with OskariLayer instead of (Map-)Layer classes and
they should be loaded using OskariLayerService instead of MapLayerService. Additional upgrade is required - [instructions can be found here](docs/upgrade/1.17.md).

### ParamHandler/ViewModifier/ModifierParams

ParamHandlers now have access to the ActionParams instance for the request. This means they can determine how to handle a parameter depending on other parameters.

## 1.16

### content-resources

Added upgrade SQLs that can be run to update database structure on version update. See [Upgrade documentation for details](docs/Upgrading.md)

Added a new commandline parameter "-Doskari.addview={file under resources/json/views}" to add a view without setup file

Added setupfiles:
* to initialize an empty base db with -Doskari.setup=create-empty-db
* to register bundles with -Doskari.setup=postgres-register-bundles

These can be used to initialize the database step-by-step. Also added an example setup for parcel application (-Doskari.setup=postgres-parcel).


### service-base / IOHelper

Added debugging methods to ignore SSL certificate errors. This can be used for existing code by adding properties (NOTE! This is global setting for all connections through IOHelper):

	oskari.trustAllCerts=true
	oskari.trustAllHosts=true

Individual connections can use these by calling IOHelper-methods:

    public static void trustAllCerts(final HttpURLConnection connection) throws IOException
    public static void trustAllHosts(final HttpURLConnection connection) throws IOException

### servlet-map

Now reads an oskari-ext.properties file from classpath to override default oskari.properties values.

### control-base GetSotkaData

Action handler has been refactored and indicators/regions listing now uses Redis for caching if available

## 1.15

### User domain object

Now has a convenience method to check if user has any of the roles in given String array (containing rolenames)

### GetWSCapabilities action route

Permitted roles for executing the action is now configurable with property 'actionhandler.GetWSCapabilitiesHandler.roles'