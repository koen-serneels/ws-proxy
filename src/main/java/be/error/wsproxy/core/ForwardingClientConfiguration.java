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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

class ForwardingClientConfiguration {

	private final ForwardingClientConfigurationSupport forwardingClientSupport = new ForwardingClientConfigurationSupport();

	private final Map<QName, String> endpointTargetUrlMapping = new HashMap<>();
	private final Map<QName, Pair<String, Integer>> httpForwardProxy = new HashMap<>();

	private final Map<QName, String> endpointProtocolMapping = new HashMap<>();
	private final Map<QName, List<ClientInterceptor>> customClientInterceptors = new HashMap<>();

	public String getEndpointTargetUrlMapping(QName qname) {
		return endpointTargetUrlMapping.get(qname);
	}

	public void setEndpointTargetUrlMapping(String reverseProxy) {
		endpointTargetUrlMapping.putAll(forwardingClientSupport.parseEndpointTargetUrlMapping(reverseProxy));
	}

	public Pair<String, Integer> getHttpForwardProxy(QName qname) {
		return httpForwardProxy.get(qname);
	}

	public void setHttpForwardProxy(String forwardProxy) {
		httpForwardProxy.putAll(forwardingClientSupport.parseForwardProxyUrl(forwardProxy));
	}

	public String getEndpointProtocolMapping(QName qname) {
		return endpointProtocolMapping.get(qname);
	}

	public void setEndpointProtocolMapping(String endpointProtocolMapping) {
		this.endpointProtocolMapping.putAll(forwardingClientSupport
				.parseEndpointProtocolMappings(endpointProtocolMapping));
	}

	public List<ClientInterceptor> getCustomClientInterceptors(QName qname) {
		return customClientInterceptors.get(qname);
	}

	public void setCustomClientInterceptors(Map<QName, List<ClientInterceptor>> clientInterceptors) {
		this.customClientInterceptors.putAll(clientInterceptors);
	}
}
