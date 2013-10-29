package be.error.wsproxy.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

class ForwardingClientConfigurationSupport {

	public Map<QName, String> parseEndpointTargetUrlMapping(String endpointTargetUrlMapping) {
		return splitQnameConfig(endpointTargetUrlMapping, "endpointTargetUrlMapping");
	}

	public Map<QName, Pair<String, Integer>> parseForwardProxyUrl(String httpForwardProxy) {
		Map<QName, Pair<String, Integer>> result = new HashMap<>();

		for (Entry<QName, String> entry : splitQnameConfig(httpForwardProxy, "httpForwardProxy").entrySet()) {
			String[] splitted = StringUtils.split(entry.getValue(), ":");
			if (splitted.length > 2) {
				throw new RuntimeException(
						"Forward proxy format should be host:port or host (port 80 assumed in that case)");
			}
			result.put(entry.getKey(),
					Pair.of(splitted[0], Integer.parseInt(splitted.length == 2 ? splitted[1] : "80")));
		}
		return result;
	}

	public Map<QName, String> parseEndpointProtocolMappings(String endpointProtocolMapping) {
		Map<QName, String> result = splitQnameConfig(endpointProtocolMapping, "endpointProtocolMapping");

		for (Entry<QName, String> entry : result.entrySet()) {
			if (!entry.getValue().equals("http") && !entry.getValue().equals("https")) {
				throw new IllegalArgumentException(
						"'endpointProtocolMapping' protocol must be http or https. Current value:" + entry.getValue()
								+ " For service:" + entry.getKey());
			}
		}
		return result;
	}

	private Map<QName, String> splitQnameConfig(String qnameConfig, String property) {
		Map<QName, String> result = new HashMap<>();

		for (String string : StringUtils.split(qnameConfig, ",")) {
			if (StringUtils.countMatches(string, "=") != 1) {
				throw new IllegalArgumentException("'" + property
						+ "' must contain configuration in the form of: {namespace}localpart=value. Current value:"
						+ string);
			}
			String[] mapping = StringUtils.split(string, "=");
			String identifier = mapping[0].trim();
			if (!identifier.contains("{") || !identifier.contains("}")) {
				throw new IllegalArgumentException("'" + property
						+ "' identifier must be in the form of: '{namespace}localpart'. Current value:" + identifier
						+ " For service:" + string);
			}
			String ns = StringUtils.substringBetween(identifier, "{", "}");
			String localPart = StringUtils.substringAfter(identifier, "}");
			String value = mapping[1].trim().toLowerCase();

			result.put(new QName(ns, localPart), value);
		}
		return result;
	}
}
