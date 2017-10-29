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
package be.error.wsproxy.interceptors.transport;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpComponentsConnection;

import be.error.wsproxy.core.ForwardingClient;

/**
 * Transfers HTTP headers from the inbound connection to the outbound connection. Can be used as an endpoint and client
 * interceptor.
 * <p>
 * Note: the 'SOAPAction' header is not transfered by this mechanism. See {@link ForwardingClient}
 *
 * @author Koen Serneels
 */
public class HttpRequestHeaderTransfererInterceptor implements ClientInterceptor, EndpointInterceptor {

	public static final String AUTHORIZATION_HEADER = "Authorization";

	private static final List<String> headersToBeTransfered = new ArrayList<>();

	private final List<Pair<String, String>> headers;

	public HttpRequestHeaderTransfererInterceptor(List<Pair<String, String>> headers) {
		this.headers = headers;
		headersToBeTransfered.add(AUTHORIZATION_HEADER);
	}

	// Endpoint interceptor methods
	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return transferHeaders(messageContext);
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
		// Do nothing
	}

	// Client interceptor methods
	@Override
	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		return transferHeaders(messageContext);
	}

	@Override
	public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	@Override
	public void afterCompletion(final MessageContext messageContext, final Exception ex) throws WebServiceClientException {
		//Do nothing
	}

	@SuppressWarnings("unchecked")
	public static List<Pair<String, String>> extractHeaders(HttpServletRequest httpServletRequest) {
		List<Pair<String, String>> result = new ArrayList<>();

		for (String headerName : (List<String>) EnumerationUtils.toList(httpServletRequest.getHeaderNames())) {
			result.add(Pair.of(headerName, httpServletRequest.getHeader(headerName)));
		}
		return result;
	}

	private boolean transferHeaders(MessageContext messageContext) {
		HttpComponentsConnection connection = (HttpComponentsConnection) TransportContextHolder.getTransportContext().getConnection();

		for (Pair<String, String> header : extractTransferableHttpHeaders()) {
			connection.getHttpPost().addHeader(header.getLeft(), header.getRight());
		}
		return true;
	}

	private List<Pair<String, String>> extractTransferableHttpHeaders() {
		List<Pair<String, String>> result = new ArrayList<>();

		for (Pair<String, String> candidate : headers) {
			for (String header : headersToBeTransfered) {
				if (candidate.getLeft().equalsIgnoreCase(header)) {
					result.add(candidate);
				}
			}
		}
		return result;
	}
}
