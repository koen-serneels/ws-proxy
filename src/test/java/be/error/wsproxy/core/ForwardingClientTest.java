package be.error.wsproxy.core;

import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.testng.annotations.Test;

@Test
public class ForwardingClientTest {

	public void testSetServiceEndpointProtocolMapping() {
		ForwardingClient forwardingClient = new ForwardingClient(new SaajSoapMessageFactory());

		forwardingClient.setServiceEndpointProtocolMapping(" ");
		forwardingClient.setServiceEndpointProtocolMapping(null);
	}
}