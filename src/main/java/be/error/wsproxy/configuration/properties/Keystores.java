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
