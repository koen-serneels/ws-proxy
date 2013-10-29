package be.error.wsproxy.core;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.context.MessageContext;

public class WebserviceIdentifierExtractorSupport {

	public static QName getWebServiceIdentifier(MessageContext messageContext) {
		DOMSource domSource = (DOMSource) messageContext.getRequest().getPayloadSource();
		if (domSource.getNode() != null && domSource.getNode().getFirstChild() != null) {
			return new QName(domSource.getNode().getNamespaceURI(), domSource.getNode().getLocalName());
		}
		throw new IllegalStateException("WebService identifier could not be determined (no payload");
	}
}
