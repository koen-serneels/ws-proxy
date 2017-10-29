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
package be.error.wsproxy;

import java.io.IOException;
import java.util.Date;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.Addressing;

import org.springframework.core.io.ClassPathResource;

public class SimpleSecuredEndpoint {

	public static void main(String args[]) throws IOException {
		// Set WSIT_HOME manually, we're only using this for testing purposes. This way we can have a dynamic path based
		// on the project location in filesystem to resolve the keystores via the WSIT configuratin in META-INF
		System.setProperty("WSIT_HOME", new ClassPathResource("").getFile().getParent() + "/../config/test-keystores/");
		Endpoint.publish("http://localhost:9999/simple", new SimpleWebServiceEndpoint());
	}

	@WebService(serviceName = "SimpleEndpoint")
	@Addressing(enabled = false, required = false)
	public static class SimpleWebServiceEndpoint {
		public Date getCurrentDateSecured(String randomParameter) {
			return new Date();
		}
	}
}
