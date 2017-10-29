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
