package be.error.wsproxy;

import java.io.IOException;
import java.util.Date;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

public class SimpleEndpoint {

	public static void main(String args[]) throws IOException {
		Endpoint.publish("http://localhost:9999/simple", new SimpleWebServiceEndpoint());
	}

	@WebService
	public static class SimpleWebServiceEndpoint {
		public Date getCurrentDate(String randomParameter) {
			return new Date();
		}
	}
}
