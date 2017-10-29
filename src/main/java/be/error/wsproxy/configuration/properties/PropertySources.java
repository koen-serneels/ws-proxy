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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import be.error.wsproxy.configuration.profiles.Local;

@Configuration
public class PropertySources {

	@Local
	static class PropertySource {

		@Bean
		@Autowired
		public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
				ApplicationContext applicationContext) {
			PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
			propertySourcesPlaceholderConfigurer.setLocation(new FileSystemResource(applicationContext.getEnvironment()
					.getProperty("project.root.dir") + "/config/wsproxy-local-config/wsproxy_local_demo.properties"));
			return propertySourcesPlaceholderConfigurer;
		}
	}
}
