package be.error.wsproxy.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import be.error.wsproxy.configuration.profiles.Local;

/**
 * When the module forwards messages to the actual target endpoint, you have to option to specify if the transport needs
 * to be secure or not. This will most likely be used for external outbound, where messages are to be sent to the
 * external target service requesting secure transport.
 * 
 * @author Koen Serneels
 */
@Configuration("endpointProtocolMappingConfiguration")
public class EndpointProtocolMapping {

	@Local
	public static class LocalConfiguration {
		@Value("${endpoint.protocol.mapping}")
		private String endpointProtocolMapping;

		// The configuration is returned as a single String to make externalization of the property easier as required
		// by other profiles (eg. JNDI for the production profile).
		@Bean
		public String serviceEndpointProtocolMapping() {
			return endpointProtocolMapping;
		}
	}
}
