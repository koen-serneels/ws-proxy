package be.error.wsproxy.interceptors;

import org.springframework.ws.server.EndpointInterceptor;

/**
 * This is a marker interface for identifying interceptors for specific service purposes. For example: logging of
 * service specific data. Other interceptors, that are generic/system should directly implement
 * {@link EndpointInterceptor}.
 * <p>
 * Service specific interceptors implementing this interface will automatically be recognized by
 * {@link DefaultInterceptorExecutor}, which apply certain default interception logic.
 * 
 * @see DefaultInterceptorExecutor
 * @author Koen Serneels
 */
public interface ServiceSpecificEndpointInterceptor extends EndpointInterceptor {

}
