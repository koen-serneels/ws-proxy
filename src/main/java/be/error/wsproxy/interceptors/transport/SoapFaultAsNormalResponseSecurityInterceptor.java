package be.error.wsproxy.interceptors.transport;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;

/**
 * Use this interceptor over {@link Wss4jSecurityInterceptor} when SOAP faults are/need also message security
 * 
 * @author Koen Serneels
 */
public class SoapFaultAsNormalResponseSecurityInterceptor extends Wss4jSecurityInterceptor {

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return super.handleResponse(messageContext, endpoint);
	}

	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		return super.handleResponse(messageContext);
	}
}