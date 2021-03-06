package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.CapabilitiesCacheService;
import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.domain.map.LayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.LayerGroupService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.util.*;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Admin insert/update of WMS map layer
 */
@OskariActionRoute("SaveLayer")
public class SaveLayerHandler extends ActionHandler {

    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();
    private WFSLayerConfigurationService wfsLayerService = ServiceFactory.getWfsLayerService();
    private PermissionsService permissionsService = ServiceFactory.getPermissionsService();
    private LayerGroupService layerGroupService = ServiceFactory.getLayerGroupService();
    private InspireThemeService inspireThemeService = ServiceFactory.getInspireThemeService();
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();

    private static final Logger log = LogFactory.getLogger(SaveLayerHandler.class);
    private static final String PARAM_LAYER_ID = "layer_id";
    private static final String PARAM_LAYER_NAME = "layerName";
    private static final String PARAM_LAYER_URL = "layerUrl";

    private static final String LAYER_NAME_PREFIX = "name_";
    private static final String LAYER_TITLE_PREFIX = "title_";

    private static final String ERROR_UPDATE_OR_INSERT_FAILED = "update_or_insert_failed";
    private static final String ERROR_NO_LAYER_WITH_ID = "no_layer_with_id:";
    private static final String ERROR_OPERATION_NOT_PERMITTED = "operation_not_permitted_for_layer_id:";
    private static final String ERROR_MANDATORY_FIELD_MISSING = "mandatory_field_missing:";
    private static final String ERROR_INVALID_FIELD_VALUE = "invalid_field_value:";


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final int layerId = saveLayer(params);
        final OskariLayer ml = mapLayerService.find(layerId);
        if(ml == null) {
            throw new ActionException("Couldn't get the saved layer from DB - id:" + layerId);
        }

        // update cache - do this before creating json!
        boolean cacheUpdated = ml.isCollection();
        // skip cache update for collections since they don't have the info
        // Skip WFS
        if(!ml.isCollection() && !OskariLayer.TYPE_WFS.equals(ml.getType()) ) {
            cacheUpdated = updateCache(ml, params.getRequiredParam("version"));
        }

        // construct response as layer json
        final JSONObject layerJSON = OskariLayerWorker.getMapLayerJSON(ml, params.getUser(), params.getLocale().getLanguage());
        if (layerJSON == null) {
            // handle error getting JSON failed
            throw new ActionException("Error constructing JSON for layer");
        }
        if(!cacheUpdated && !ml.isCollection() && !OskariLayer.TYPE_WFS.equals(ml.getType()) ) {
            // Cache update failed, no biggie
            JSONHelper.putValue(layerJSON, "warn", "metadataReadFailure");
        }
        ResponseHelper.writeResponse(params, layerJSON);
    }

    private int saveLayer(final ActionParameters params) throws ActionException {

        // layer_id can be string -> external id!
        final String layer_id = params.getHttpParam(PARAM_LAYER_ID);

        try {
            // ************** UPDATE ************************
            if (layer_id != null) {

                final OskariLayer ml = mapLayerService.find(layer_id);
                if (ml == null) {
                    // layer wasn't found
                    throw new ActionException(ERROR_NO_LAYER_WITH_ID + layer_id);
                }
                if (!permissionsService.hasEditPermissionForLayerByLayerId(params.getUser(), ml.getId())) {
                    throw new ActionDeniedException(ERROR_OPERATION_NOT_PERMITTED + layer_id);
                }

                handleRequestToMapLayer(params, ml);

                ml.setUpdated(new Date(System.currentTimeMillis()));
                mapLayerService.update(ml);

                log.debug(ml);

                return ml.getId();
            }

            // ************** INSERT ************************
            else {

                if (!permissionsService.hasAddLayerPermission(params.getUser())) {
                    throw new ActionDeniedException(ERROR_OPERATION_NOT_PERMITTED + layer_id);
                }

                final OskariLayer ml = new OskariLayer();
                final Date currentDate = new Date(System.currentTimeMillis());
                ml.setCreated(currentDate);
                ml.setUpdated(currentDate);
                handleRequestToMapLayer(params, ml);
                int id = mapLayerService.insert(ml);
                ml.setId(id);

                if(ml.isCollection()) {
                    // update the name with the id for permission mapping
                    ml.setName(ml.getId() + "_group");
                    mapLayerService.update(ml);
                }
                // Wfs
                if(OskariLayer.TYPE_WFS.equals(ml.getType())) {
                    final WFSLayerConfiguration wfsl = new WFSLayerConfiguration();
                    wfsl.setDefaults();
                    wfsl.setLayerId(Integer.toString(id));
                    handleRequestToWfsLayer(params, wfsl);
                    int idwfsl = wfsLayerService.insert(wfsl);
                    wfsl.setId(idwfsl);

                }

                final String[] externalIds = params.getHttpParam("viewPermissions", "").split(",");

                addPermissionsForRoles(ml, params.getUser(), externalIds);

                // update keywords
                GetLayerKeywords glk = new GetLayerKeywords();
                glk.updateLayerKeywords(id, ml.getMetadataId());

                return ml.getId();
            }

        } catch (Exception e) {
            if (e instanceof ActionException) {
                throw (ActionException) e;
            } else {
                throw new ActionException(ERROR_UPDATE_OR_INSERT_FAILED, e);
            }
        }
    }

    private boolean updateCache(OskariLayer ml, final String version) throws ActionException {
        if(ml == null) {
            return false;
        }
        if(ml.isCollection()) {
            // just be happy for collection layers, nothing to do
            return true;
        }
        if(version == null) {
            // check this here since it's not always required (for collection layers)
            throw new ActionParamsException("Version is required!");
        }
        // retrieve capabilities
        final String url = getSingleLayerUrl(ml.getUrl());
        CapabilitiesCache cc = null;
        try {
            cc = capabilitiesService.find(ml.getId());
            boolean isNew = false;
            if (cc == null) {
                cc = new CapabilitiesCache();
                cc.setLayerId(ml.getId());
                isNew = true;
            }
            cc.setVersion(version);
            if(OskariLayer.TYPE_WMS.equals(ml.getType())) {
                final String capabilitiesXML = GetWMSCapabilities.getResponse(url, ml.getUsername(), ml.getPassword());
                cc.setData(capabilitiesXML);
            }
            else if(OskariLayer.TYPE_WMTS.equals(ml.getType())) {
                // TODO: maybe it a bit more elegant solution
                final String capabilitiesXML = IOHelper.getURL(url + "?service=WMTS&request=GetCapabilities", ml.getUsername(), ml.getPassword());
                cc.setData(capabilitiesXML);
            }

            // update cache by updating db
            if (isNew) {
                capabilitiesService.insert(cc);
            } else {
                capabilitiesService.update(cc);
            }
            // flush cache, otherwise only db is updated but code retains the old cached version
            WebMapServiceFactory.flushCache(ml.getId());
        } catch (Exception ex) {
            log.info(ex, "Error updating capabilities: ", cc, "from URL:", url);
            return false;
        }
        return true;
    }

    private String getSingleLayerUrl(String wmsUrl) {
        if(wmsUrl == null) {
            return null;
        }
        //check if comma separated urls
        if (wmsUrl.indexOf(",http:") > 0) {
            wmsUrl = wmsUrl.substring(0, wmsUrl.indexOf(",http:"));
        }
        return wmsUrl;

    }

    private void handleRequestToMapLayer(final ActionParameters params, OskariLayer ml) throws ActionException {

        HttpServletRequest request = params.getRequest();

        if(ml.getId() == -1) {
            // setup type and parent for new layers only
            ml.setType(params.getHttpParam("layerType"));
            ml.setParentId(params.getHttpParam("parentId", -1));
        }

        // organization id
        final LayerGroup group = layerGroupService.find(params.getHttpParam("groupId", -1));
        ml.addGroup(group);

        // get names and descriptions
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String nextName = paramNames.nextElement();
            if (nextName.indexOf(LAYER_NAME_PREFIX) == 0) {
                ml.setName(nextName.substring(LAYER_NAME_PREFIX.length()).toLowerCase(), params.getHttpParam(nextName));
            } else if (nextName.indexOf(LAYER_TITLE_PREFIX) == 0) {
                ml.setTitle(nextName.substring(LAYER_TITLE_PREFIX.length()).toLowerCase(), params.getHttpParam(nextName));
            }
        }

        InspireTheme theme = inspireThemeService.find(params.getHttpParam("inspireTheme", -1));
        ml.addInspireTheme(theme);

        ml.setBaseMap(ConversionHelper.getBoolean(params.getHttpParam("isBase"), false));

        if(ml.isCollection()) {
            // ulr is needed for permission mapping, name is updated after we get the layer id
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return;
        }

        ml.setName(params.getRequiredParam(PARAM_LAYER_NAME, ERROR_MANDATORY_FIELD_MISSING + PARAM_LAYER_NAME));
        final String url = validateUrl(params.getRequiredParam(PARAM_LAYER_URL, ERROR_MANDATORY_FIELD_MISSING + PARAM_LAYER_URL));
        ml.setUrl(url);

        ml.setOpacity(params.getHttpParam("opacity", ml.getOpacity()));
        ml.setStyle(params.getHttpParam("style", ml.getStyle()));
        ml.setMinScale(ConversionHelper.getDouble(params.getHttpParam("minScale"), ml.getMinScale()));
        ml.setMaxScale(ConversionHelper.getDouble(params.getHttpParam("maxScale"), ml.getMaxScale()));

        ml.setLegendImage(params.getHttpParam("legendImage", ml.getLegendImage()));
        ml.setMetadataId(params.getHttpParam("metadataId", ml.getMetadataId()));
        ml.setTileMatrixSetId(params.getHttpParam("tileMatrixSetId"));
        ml.setTileMatrixSetData(params.getHttpParam("tileMatrixSetData"));

        final String gfiContent = request.getParameter("gfiContent");
        if (gfiContent != null) {
            // Clean GFI content
            final String[] tags = PropertyUtil.getCommaSeparatedList("gficontent.whitelist");
            HashMap<String,String[]> attributes = new HashMap<String, String[]>();
            HashMap<String[],String[]> protocols = new HashMap<String[], String[]>();
            String[] allAttributes = PropertyUtil.getCommaSeparatedList("gficontent.whitelist.attr");
            if (allAttributes.length > 0) {
                attributes.put(":all",allAttributes);
            }
            List<String> attrProps = PropertyUtil.getPropertyNamesStartingWith("gficontent.whitelist.attr.");
            for (String attrProp : attrProps) {
                String[] parts = attrProp.split("\\.");
                if (parts[parts.length-2].equals("protocol")) {
                    protocols.put(new String[]{parts[parts.length-3],parts[parts.length-1]},PropertyUtil.getCommaSeparatedList(attrProp));
                } else {
                    attributes.put(parts[parts.length-1],PropertyUtil.getCommaSeparatedList(attrProp));
                }
            }
            ml.setGfiContent(RequestHelper.cleanHTMLString(gfiContent, tags, attributes, protocols));
        }

        ml.setUsername(params.getHttpParam("username", ml.getUsername()));
        ml.setPassword(params.getHttpParam("password", ml.getPassword()));

        ml.setSrs_name(params.getHttpParam("srs_name",ml.getSrs_name()));
        ml.setVersion(params.getHttpParam("version",ml.getVersion()));

        ml.setRealtime(ConversionHelper.getBoolean(params.getHttpParam("realtime"), ml.getRealtime()));
        ml.setRefreshRate(ConversionHelper.getInt(params.getHttpParam("refreshRate"), ml.getRefreshRate()));

        if(OskariLayer.TYPE_WMS.equals(ml.getType())) {
            handleWMSSpecific(params, ml);
        }
        else if(OskariLayer.TYPE_WFS.equals(ml.getType())) {
            handleWFSSpecific(params, ml);
        }
        else if(OskariLayer.TYPE_WMTS.equals(ml.getType())) {
            handleWMTSSpecific(params, ml);
        }
    }
    private void handleRequestToWfsLayer(final ActionParameters params, WFSLayerConfiguration wfsl) throws ActionException {

        wfsl.setGML2Separator(ConversionHelper.getBoolean(params.getHttpParam("GML2Separator"),wfsl.isGML2Separator()));
        wfsl.setGMLGeometryProperty(params.getHttpParam("GMLGeometryProperty"));
        wfsl.setGMLVersion(params.getHttpParam("GMLVersion"));
        wfsl.setSRSName(params.getHttpParam("SRSName"));
        wfsl.setWFSVersion(params.getHttpParam("WFSVersion"));
        wfsl.setFeatureElement(params.getHttpParam("featureElement"));
        wfsl.setFeatureNamespace(params.getHttpParam("featureNamespace"));
        wfsl.setFeatureNamespaceURI(params.getHttpParam("featureNamespaceURI"));
        wfsl.setFeatureParamsLocales(params.getHttpParam("featureParamsLocales"));
        wfsl.setFeatureType(params.getHttpParam("featureType"));
        wfsl.setGeometryNamespaceURI(params.getHttpParam("geometryNamespaceURI"));
        wfsl.setGeometryType(params.getHttpParam("geometryType"));
        wfsl.setGetFeatureInfo(ConversionHelper.getBoolean(params.getHttpParam("getFeatureInfo"), wfsl.isGetFeatureInfo()));
        wfsl.setGetHighlightImage(ConversionHelper.getBoolean(params.getHttpParam("getHighlightImage"), wfsl.isGetHighlightImage()));
        wfsl.setGetMapTiles(ConversionHelper.getBoolean(params.getHttpParam("getMapTiles"), wfsl.isGetMapTiles()));

        wfsl.setLayerName(params.getHttpParam("layerName"));
        wfsl.setMaxFeatures(ConversionHelper.getInt(params.getHttpParam("maxFeatures"),wfsl.getMaxFeatures()));
        wfsl.setOutputFormat(params.getHttpParam("outputFormat"));
        wfsl.setSelectedFeatureParams(params.getHttpParam("selectedFeatureParams"));
        wfsl.setTileBuffer(params.getHttpParam("tileBuffer"));
        wfsl.setTileRequest(ConversionHelper.getBoolean(params.getHttpParam("tileRequest"), wfsl.isTileRequest()));

    }
    private void handleWMSSpecific(final ActionParameters params, OskariLayer ml) throws ActionException {

        HttpServletRequest request = params.getRequest();
        final String xslt = request.getParameter("xslt");
        if(xslt != null) {
            // TODO: some validation of XSLT data
            ml.setGfiXslt(xslt);
        }
        ml.setGfiType(params.getHttpParam("gfiType", ml.getGfiType()));
    }

    private void handleWMTSSpecific(final ActionParameters params, OskariLayer ml) throws ActionException {
        ml.setTileMatrixSetId(params.getHttpParam("matrixSetId", ml.getTileMatrixSetId()));
        ml.setTileMatrixSetData(params.getHttpParam("matrixSet", ml.getTileMatrixSetData()));
    }

    private void handleWFSSpecific(final ActionParameters params, OskariLayer ml) throws ActionException {
        // these are only in insert
        ml.setSrs_name(params.getHttpParam("SRSName",ml.getSrs_name()));
        ml.setVersion(params.getHttpParam("WFSVersion",ml.getVersion()));
    }
    private String validateUrl(final String url) throws ActionParamsException {
        try {
            // check that it's a valid url by creating an URL object...
            new URL(getSingleLayerUrl(url));
        } catch (MalformedURLException e) {
            throw new ActionParamsException(ERROR_INVALID_FIELD_VALUE + PARAM_LAYER_URL);
        }
        return url;
    }

    private void addPermissionsForRoles(final OskariLayer ml, final User user, final String[] externalIds) {

        OskariLayerResource res = new OskariLayerResource(ml);
        // insert permissions
        for (String externalId : externalIds) {
            final long extId = ConversionHelper.getLong(externalId, -1);
            if (extId != -1 && user.hasRoleWithId(extId)) {
                Permission permission = new Permission();
                permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                permission.setExternalId(externalId);
                permission.setType(Permissions.PERMISSION_TYPE_VIEW_LAYER);
                res.addPermission(permission);

                permission = new Permission();
                permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                permission.setExternalId(externalId);
                permission.setType(Permissions.PERMISSION_TYPE_EDIT_LAYER);
                res.addPermission(permission);
            }
        }
        permissionsService.saveResourcePermissions(res);

    }
}
