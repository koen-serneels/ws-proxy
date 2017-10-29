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
import be.error.wsproxy.core.ForwardingClient;

/**
 * When the ws-proxy module receives a request it needs to find out to where the request should be forwarded to. The
 * standard scenario is that an internal client uses the module to access an external service. In such case the client
 * should configure this module as an http forward proxy. This way the module can deduct the target hostname from the
 * http host header and no further configuration is required. In other words, the client will submit the request to the
 * module but the clients http stack will provide the actual host:port of the target as http header which is then
 * extracted by this module and used to forward the request.<br>
 * <p>
 * There are however two other scenario's:
 * 
 * <ul>
 * <li>For some reason the internal client does not know the endpoint URL but simply wants to forward the message to the
 * module, the module should then know to where the message should be forward to. In that case one can manually register
 * the target URL here for that specific service. Remember, the target URL can be anything, it can be the actual URL of
 * the endpoint, it can also be the URL of a reverse proxy which will then deal with further message delivery.</li>
 * <li>We have an external client accessing our internal services. In that case the target URL of the internal service
 * needs to be configured here.</li>
 * </ul>
 * 
 * A service (either internal or external) is identified by the payload root element and its namespace. Eg.
 * "{http://service.error.be}localPart. Together with the endpoint URL, for example: "
 * {http://service.error.be}localPart=http://internalhost/someEndpoint". When an endpoint hosts multiple services, you
 * can have multiple services pointing to the same endpoint. Multiple entries are comma separated.
 * 
 * @see ForwardingClient
 * @author Koen Serneels
 */
@Configuration("endpointTargetUrlMappingConfiguration")
public class EndpointTargetUrlMapping {

	@Local
	public static class LocalConfiguration {

		@Value("${endpoint.target.url.mapping}")
		private String endpointTargetUrlMapping;

		@Bean
		public String endpointTargetUrlMapping() {
			return endpointTargetUrlMapping;
		}
	}
}
