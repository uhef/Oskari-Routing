package fi.nls.oskari.work.fe;

import java.util.Map;
import java.io.IOException;

import fi.nls.oskari.work.RequestResponse;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.filter.Filter;

import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.pojo.Location;

public class FERequestResponse implements RequestResponse {
    Map<Resource, SimpleFeatureCollection> response;
    private Filter filter;
    Location location;
    Resource featureIri;

    public Map<Resource, SimpleFeatureCollection> getResponse() {
        return response;
    }

    public void setResponse(Map<Resource, SimpleFeatureCollection> response) {
        this.response = response;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;

    }

    public Filter getFilter() {
        return filter;
    }

    public void setLocation(Location location) {
        this.location = location;

    }

    public Location getLocation() {
        return location;
    }

    public Resource getFeatureIri() {
        return featureIri;
    }

    public void setFeatureIri(Resource featureIri) {
        this.featureIri = featureIri;
    }
    
    public void flush() throws IOException {
        
    }

}
