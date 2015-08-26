package pnnl.goss.fusiondb.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.felix.http.api.ExtHttpService;

import pnnl.goss.core.server.TokenIdentifierMap;

import com.google.gson.JsonObject;

/**
 * This filter tests that a user has logged in before allowing
 * access to the requested resource.  It does this by using a
 * {@link TokenIdentifierMap} based service that will check the
 * ip address and the pressence of a valid token.
 *
 * If a valid token is present then the request will modified to
 * include an "identifier" parameter that can be used in a web request
 * to authenticate a user's permissions.
 *
 * @author Craig Allwardt
 *
 */
@Component
public class LoggedInFilter implements Filter
{

	@ServiceDependency
    private volatile ExtHttpService httpService;

	@ServiceDependency
	private volatile TokenIdentifierMap idMap;

    @Start
    public void start() throws ServletException{
    	System.out.println("Starting "+this.getClass().getName());
    	try {
			httpService.registerFilter(this, "/fusion/api/.*",  null,  100,  null);
		} catch (ServletException e) {
			e.printStackTrace();
			throw e;
		}

    }

    @Stop
    public void stop(){
    	httpService.unregisterFilter(this);
    }

    @Override
    public void init(FilterConfig config)
        throws ServletException
    {
        System.out.println("Initializing filter with config: "+config);
    }

    
     /* This function is designed to validate that a user has been logged into
     * the system and made a request within a period of time.  The time is
     * not determined in this class but in the {@link TokenIdentifiedMap} service.
     * In addition the token and ip address will be checked to make sure the
     * origin of the request is from the same ip.
     *
     * If the request is a GET request then the header AuthToken must be present
     * with a validated token.  If a POST request then the AuthToken can either
     * be present in the header or in a json body element.
     *
     * If the AuthToken is valid then an 'identifier' parameter will be set on the
     * request before it is sent to the next filter.
     *
     * If the AuthToken is not valid or is invalid then 401 header is set and an
     * error message is produced.
     *
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
    	HttpServletRequest httpReq = (HttpServletRequest) req;
    	MultiReadHttpServletRequestWrapper wrapper = new MultiReadHttpServletRequestWrapper(httpReq);
    	//checking login
    	JsonObject requestBody = WebUtil.getRequestJsonBody(wrapper);
    	wrapper.setAttribute("identifier", "system");
    	wrapper.setRequest(httpReq);
    	chain.doFilter(wrapper, res);
    	System.out.println("done");
    }

	@Override
	public void destroy() {
		System.out.println("Destroying filter.");
	}
}