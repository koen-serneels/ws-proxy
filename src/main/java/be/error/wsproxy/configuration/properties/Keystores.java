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

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import be.error.wsproxy.configuration.core.Keystore;
import be.error.wsproxy.configuration.profiles.Local;

@Configuration
public class Keystores {

	@Local
	public static class LocalConfiguration {

		@Value("${keystore}")
		private String keystore;
		@Value("${keystore.password}")
		private String keystorePassword;
		@Value("${key.alias}")
		private String keyAlias;
		@Value("${key.password}")
		private String keyPassword;
		@Value("${truststore}")
		private String truststore;
		@Value("${truststore.password}")
		private String truststorePassword;

		@Bean
		public Keystore keystore() {
			return new Keystore(new FileSystemResource(keystore), keystorePassword, Pair.of(keyAlias, keyPassword));
		}

		@Bean
		public Keystore truststore() {
			return new Keystore(new FileSystemResource(truststore), truststorePassword);
		}
	}
}
