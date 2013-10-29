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
