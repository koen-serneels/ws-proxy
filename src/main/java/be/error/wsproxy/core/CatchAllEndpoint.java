/*-
 * #%L
 * Home Automation
 * %%
 * Copyright (C) 2016 - 2017 Koen Serneels
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package be.error.wsproxy.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import be.error.wsproxy.interceptors.ServiceSpecificEndpointInterceptor;

/**
 * This is the catch-all {@link Endpoint} which will forward requests to the target service using
 * {@link ForwardingClient}. If you need service specific actions on the inbound side, you can add them using
 * {@link ServiceSpecificEndpointInterceptor} for this endpoint. Remember, as being a proxy, this endpoint has two
 * purposes; it is both a Webservice (endpoint) but also a Webservice client for the actual target. Because of this
 * there are two filter chains involved, the ones defined on the endpoint itself (inbound) and the ones defined on
 * {@link ForwardingClient#setCustomClientInterceptors(java.util.Map)} (outbound).
 * <p>
 * If an error occurs during forwarding (ie. the response retrieved was not a valid SOAP message) a runtime exception
 * will be thrown and converted to a ({@link ForwardingEndpointTargetException}). The exception will include the HTTP
 * code &amp; reason and the the body (if available) of the upstream response as exception message. This will result in a
 * SOAP fault being returned to the caller, containing the complete message. Doing this we offer an extra service to
 * callers making sure that in case of off-spec return values from upstream servers they still get a nice SOAP valid
 * reply: a SOAP fault. This also means that the handleFault of configured interceptors for this endpoint will be
 * triggered.
 * 
 * @see ForwardingClient
 * @author Koen Serneels
 */

@Endpoint
public class CatchAllEndpoint implements MessageEndpoint {

	private static final Logger LOG = Logger.getLogger(CatchAllEndpoint.class);

	@Autowired
	private ForwardingClient forwardingClient;

	@Override
	public void invoke(final MessageContext messageContext) {
		try {
			HttpServletRequest httpServletRequest = ((HttpServletConnection) TransportContextHolder
					.getTransportContext().getConnection()).getHttpServletRequest();
			messageContext.setResponse(forwardingClient.forward(
					WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext),
					messageContext.getRequest(), httpServletRequest));
		} catch (RuntimeException runtimeException) {
			throw (createAndLogEndpointException(runtimeException, "Error communicating with upstream server."));
		}
	}

	private RuntimeException createAndLogEndpointException(Throwable exception, String reason) {
		ForwardingEndpointTargetException forwardingEndpointTargetException = new ForwardingEndpointTargetException(
				reason + " Exception:[" + ExceptionUtils.getStackTrace(exception) + "]", exception);
		LOG.error("Had exception while forwarding request to target endpoint:" + forwardingEndpointTargetException);
		return forwardingEndpointTargetException;
	}
}
