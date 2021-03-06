package fi.nls.oskari.control;

import java.util.HashMap;
import java.util.Properties;
import java.util.ServiceLoader;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Router for Ajax Requests made by the Oskari Map Framework.
 */
public class ActionControl {
    /**
     * Name for the Http parameter that speficies the route key
     */
    public final static String PARAM_ROUTE = "action_route";
    
    private final static Logger log = LogFactory.getLogger(ActionControl.class);
	private static final HashMap<String, ActionHandler> actions = new HashMap<String, ActionHandler>();

    /**
     * Adds an action route handler with given route key
     * @param action route key
     * @param handlerClassName name of the class extending #ActionHandler that will be used to handle the route
     */
    public static void addAction(final String action, final String handlerClassName) {
        try {
            final Class clazz = Class.forName(handlerClassName);
            final ActionHandler handler = (ActionHandler) clazz.newInstance();
            addAction(action, handler);
        } catch (Exception e) {
            log.error(e, "Error adding handler for action:", action, " - class:", handlerClassName);
        }
    }
    /**
     * Adds an action route handler with given route key
     * @param action route key
     * @param handler handler for the route
     */
    public static void addAction(final String action, final ActionHandler handler) {
        try {
            handler.init();
            actions.put(action, handler);
            log.debug("Action added", action,"=", handler.getClass().getCanonicalName());
        }
        catch (Exception ex) {
            log.error(ex, "Action init failed! Skipping", action,"=", handler.getClass().getCanonicalName());
        }
    }

    /**
     * Adds all ActionHandlers defined on the properties with the property key as route key
     * @param props
     */
	public static void addActions(final Properties props) {
	    for(Object key : props.keySet()) {
	        final String handlerClassName = (String) props.get(key);
	        addAction((String) key, handlerClassName);
	    }
	}

    /**
     * Uses ServiceLoader to find all ActionHandlers in classpath. Adds them as handlers with the route key
     * returned by getName() method.
     */
    public static void addDefaultControls() {

        ServiceLoader<ActionHandler> impl = ServiceLoader.load(ActionHandler.class);

        for (ActionHandler loadedImpl : impl) {
            if ( loadedImpl != null ) {
                addAction(loadedImpl.getName(), loadedImpl);
            }
        }
    }

    /**
     * Routes a request to a handler matching the route key
     * @param action route key
     * @param params parameters describing the request
     * @throws ActionException if route is not registered or something goes wrong while handling the request
     */
	public static void routeAction(final String action, final ActionParameters params) throws ActionException {
		if (actions.isEmpty()) {
		    addDefaultControls();
		}
        if (actions.containsKey(action)) {
            try {
                actions.get(action).handleAction(params);
            } catch (Exception ex) {
                if(ex instanceof ActionException) {
                    throw (ActionException) ex;
                }
                else {
                    ex.printStackTrace();
                    throw new ActionException("Unhandled exception occured", ex);
                }
            }
        } else {
            throw new ActionParamsException("ActionRoute not defined: " + action);
        }
	}

    /**
     * Convenient way to check if we have an implementation for the route registered
     * @param action
     * @return true if we have a handler registered for the route
     */
    public static boolean hasAction(final String action) {
        if(action == null) {
            return false;
        }
        if (actions.isEmpty()) {
            addDefaultControls();
        }
        return actions.containsKey(action);
    }

    /**
     * Cleanup method. Calls teardown on all registered handlers.
     */
    public static void teardown() {
        for( ActionHandler h : actions.values()) {
            try {
                h.teardown();
            }
            catch (Exception ex) {
                log.error(ex, "Action teardown failed! Skipping", h.getName(),"=", h.getClass().getCanonicalName());
            }
        }
    }
}
