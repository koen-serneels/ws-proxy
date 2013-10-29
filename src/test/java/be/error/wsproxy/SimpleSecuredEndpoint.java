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
		public Date getCurrentDate(String randomParameter) {
			return new Date();
		}
	}
}
