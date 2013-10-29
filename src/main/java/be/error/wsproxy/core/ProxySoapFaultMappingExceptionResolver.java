package be.error.wsproxy.core;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

/**
 * We need a custom {@link SoapFaultMappingExceptionResolver} since the default one does allow to change the soap
 * faultactor
 * 
 * @author Koen Serneels
 */
public class ProxySoapFaultMappingExceptionResolver extends SoapFaultMappingExceptionResolver {

	private static final String PROXY_ACTOR = "http://proxy.ws.";
	private static final String TARGET_WS_ACTOR = "http://target.proxy.";

	private final String domain;

	public ProxySoapFaultMappingExceptionResolver(String domain) {
		this.domain = domain;
	}

	/**
	 * The soap fault actor is {@link #TARGET_WS_ACTOR} when the exception was a
	 * {@link ForwardingEndpointTargetException}. In that case the error is triggered by a fault communicating with the
	 * target webservice. In all other cases the actor is {@link #PROXY_ACTOR} indicating it is a problem from the proxy
	 * module itself.
	 */
	@Override
	protected void customizeFault(Object endpoint, Exception exception, SoapFault fault) {
		if (exception instanceof ForwardingEndpointTargetException) {
			fault.setFaultActorOrRole(TARGET_WS_ACTOR + domain);
		} else {
			fault.setFaultActorOrRole(PROXY_ACTOR + domain);
		}
	}
}