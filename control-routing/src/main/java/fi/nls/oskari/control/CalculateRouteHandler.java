package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("CalculateRoute")
public class CalculateRouteHandler extends ActionHandler {
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        ResponseHelper.writeResponse(params, "Hello " + params.getUser().getFirstname());
    }
}
