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
package be.error.wsproxy.interceptors;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;

/**
 * This interceptor will only delegate if no service specific interceptors are configured. This is useful when accessing
 * a new target service via the proxy module for which no extra configuration has been foreseen. From the moment service
 * specific interceptors are configured, this interceptor will be skipped automatically.
 * 
 * @author Koen Serneels
 */
public class DefaultInterceptorExecutor implements EndpointInterceptor {

	@Autowired
	private MessageDispatcher messageDispatcher;
	private final EndpointInterceptor delegate;

	public DefaultInterceptorExecutor(EndpointInterceptor delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		if (shouldInvoke(messageContext)) {
			return delegate.handleFault(messageContext, endpoint);
		}
		return true;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		if (shouldInvoke(messageContext)) {
			return delegate.handleRequest(messageContext, endpoint);
		}
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		if (shouldInvoke(messageContext)) {
			return delegate.handleResponse(messageContext, endpoint);
		}
		return true;
	}

	private boolean shouldInvoke(MessageContext messageContext) throws Exception {
		List<EndpointMapping> endpointMappings = messageDispatcher.getEndpointMappings();
		// There should be only one endpoint mapped in the module: check it
		Assert.isTrue(endpointMappings != null && endpointMappings.size() == 1);

		EndpointMapping endpointMapping = endpointMappings.iterator().next();
		for (EndpointInterceptor endpointInterceptor : endpointMapping.getEndpoint(messageContext).getInterceptors()) {
			// Check to see if the interceptor is directly an instance of a service specific interceptor.
			// If so, we don't have to fall back to our default delegate
			if (endpointInterceptor instanceof ServiceSpecificEndpointInterceptor) {
				return false;
			}

			// Check to see if the interceptor is decorated by DelegatingSmartEndpointInterceptor
			// Unfortunately there is no standard API way of detecting this, so we need some reflection.
			// If so, check that the decorated instance is instance of a service specific interceptor.
			// If so, we don't have to fall back to our default delegate
			if (endpointInterceptor instanceof DelegatingSmartEndpointInterceptor) {
				if (FieldUtils.readField(endpointInterceptor, "delegate", true) instanceof ServiceSpecificEndpointInterceptor) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
		// Do nothing
	}
}
