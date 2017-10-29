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
