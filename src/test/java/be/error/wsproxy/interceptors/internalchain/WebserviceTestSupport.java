package be.error.wsproxy.interceptors.internalchain;

import java.io.ByteArrayInputStream;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class WebserviceTestSupport {

	private static SaajSoapMessageFactory webServiceMessageFactory;

	static {
		SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory();
		saajSoapMessageFactory.afterPropertiesSet();
		webServiceMessageFactory = saajSoapMessageFactory;
	}

	public static WebServiceMessage createSoapWebServiceMessage(String xml) {
		try {
			return webServiceMessageFactory.createWebServiceMessage(new ByteArrayInputStream(xml.getBytes()));
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
