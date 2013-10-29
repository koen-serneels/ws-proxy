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
