package fi.nls.test.control;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.MapBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.mockito.exceptions.base.MockitoAssertionError;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * @author SMAKINEN
 * Base junit test class for ActionHandlers responding with JSON. Convenience methods for mocking ActionParams and getting the written response
 */
public class JSONActionRouteTest {

    private StringWriter response = new StringWriter();
    private User guestUser = new GuestUser();
    private User loggedInUser = null;
    private User adminUser = null;

    @Before
    public void jsonActionRouteSetUp() throws Exception {
        response = new StringWriter();
    }

    @After
    public void jsonActionRouteTeardown() throws Exception {
        response.close();
    }

    /**
     * Creates an empty ActionParams with no http parameters and GuestUser
     * @return
     */
    public ActionParameters createActionParams() {
        return createActionParams(getGuestUser());
    }

    public ActionParameters createActionParams(final InputStream payload) {
        return createActionParams(getGuestUser(), payload);
    }

    /**
     * Creates an empty ActionParams with no http parameters and given User
     * @return
     */
    public ActionParameters createActionParams(final User user) {
        return createActionParams(new HashMap<String, String>(), user);
    }

    public ActionParameters createActionParams(final User user, final InputStream payload) {
        return createActionParams(new HashMap<String, String>(), user, payload);
    }

    /**
     * Creates an ActionParams with given http parameters and GuestUser
     * @return
     */
    public ActionParameters createActionParams(final Map<String, String> parameters) {
        return createActionParams(parameters, getGuestUser());
    }

    public ActionParameters createActionParams(final Map<String, String> parameters, final InputStream payload) {
        return createActionParams(parameters, getGuestUser(), payload);
    }

    /**
     * Creates an ActionParams with given http parameters and User
     * @return
     */
    public ActionParameters createActionParams(final Map<String, String> parameters, final User user) {
        return createActionParams(parameters, user, null);
    }
    public ActionParameters createActionParams(final Map<String, String> parameters, final User user, final InputStream payload) {
        final ActionParameters params = new ActionParameters();
        // request params
        HttpServletRequest req = mock(HttpServletRequest.class);
        for(String key : parameters.keySet()) {
            when(req.getParameter(key)).thenReturn(parameters.get(key));
        }

        doReturn(new Vector(parameters.keySet()).elements()).when(req).getParameterNames();
        if(!response.toString().isEmpty()) {
            fail("Creating new ActionParams, but response already has content: " + response.toString());
        }
        // mock possible payload inputstream
        if(payload != null) {
            try {
                ServletInputStream wrapper = new ServletInputStream() {
                    @Override
                    public int read() throws IOException {
                        return payload.read();
                    }
                };
                doReturn(wrapper).when(req).getInputStream();
            }
            catch (IOException ignored ) {}
        }
        // response handler
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter output = new PrintWriter(response);
        try {
            doReturn(output).when(resp).getWriter();
        }
        catch (IOException ignored ) {}

        params.setRequest(req);
        params.setResponse(resp);
        params.setUser(user);
        params.setLocale(Locale.ENGLISH);

        return params;
    }
    public void verifyResponseNotWritten(final ActionParameters params) {
        try {
            verify(params.getResponse(), never()).getWriter();
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting response was not written, but it was!");
        }
        catch (IOException ignore) {}
    }

    public void verifyResponseContent(final JSONObject expectedResult) {
        final JSONObject actualResponse = getResponseJSON();
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        }
        finally {
            if(!success) {
                try {
                    System.out.println(">>>>>> Expected:\n" + expectedResult.toString(2));
                    System.out.println("  =======  Actual:");
                    System.out.println(actualResponse.toString(2));
                    System.out.println("<<<<<<<<<<<");
                } catch (JSONException ignored) {
                    System.out.println("Couldn't print out the jsons");
                }
            }
        }
    }

    public void verifyResponseContent(final JSONArray expectedResult) {
        final JSONArray actualResponse = getResponseJSONArray();
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        }
        finally {
            if(!success) {
                try {
                    System.out.println(">>>>>> Expected:\n" + expectedResult.toString(2));
                    System.out.println("  =======  Actual:");
                    System.out.println(actualResponse.toString(2));
                    System.out.println("<<<<<<<<<<<");
                } catch (JSONException ignored) {
                    System.out.println("Couldn't print out the jsons");
                }
            }
        }
    }

    public void verifyResponseContent(final String expectedResult) {
        final String actualResult = getResponseString();
        assertEquals("Response should match expected", expectedResult, actualResult);
    }

    public void verifyResponseWritten(final ActionParameters params) {
        try {
            verify(params.getResponse(), times(1)).getWriter();
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting response to be written, but it wasn't!");
        }
        catch (IOException ignore) {}
    }

    /**
     * Returns the JSONObject that the route has written in the response
     * @return
     */
    public JSONObject getResponseJSON() {
        return JSONHelper.createJSONObject(getResponseString());
    }

    /**
     * Returns the JSONObject that the route has written in the response
     * @return
     */
    public JSONArray getResponseJSONArray() {
        return JSONHelper.createJSONArray(getResponseString());
    }

    /**
     * Returns the text that the route has written in the response
     * @return
     */
    public String getResponseString() {
        return response.toString();
    }

    public User getGuestUser() {
        return guestUser;
    }

    public User getLoggedInUser() {
        if(loggedInUser == null) {
            loggedInUser = new User();
            loggedInUser.setId(123);
            loggedInUser.setEmail("test@oskari.org");
            loggedInUser.setFirstname("Test");
            loggedInUser.setLastname("Oskari");
            loggedInUser.setScreenname("Ozkari");
            loggedInUser.setUuid("my uuid is secrets");
        }
        return loggedInUser;
    }
    public User getAdminUser() {
        if(adminUser == null) {
            adminUser = mock(User.class);
            // mock as admin
            doReturn(true).when(adminUser).isAdmin();
        }
        return adminUser;
    }

    public MapBuilder buildParams() {
        return MapBuilder.build();
    }
}
