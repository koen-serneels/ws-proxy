package be.error.wsproxy.core;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class MessageSupport {

	public static Node transformSourceToNode(Source message) throws TransformerConfigurationException,
			TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMResult domResult = new DOMResult();
		transformer.transform(message, domResult);
		return domResult.getNode();
	}

	public static String transformSourceToString(Source message) throws TransformerConfigurationException,
			TransformerException, UnsupportedEncodingException {
		return transformNodeToString(transformSourceToNode(message));
	}

	public static String transformNodeToString(Node node) throws TransformerConfigurationException,
			TransformerException, UnsupportedEncodingException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(node), new StreamResult(byteArrayOutputStream));

		// We assume UTF8 if no encoding information is present

		return new String(byteArrayOutputStream.toByteArray(), node.getOwnerDocument() == null
				|| node.getOwnerDocument().getXmlEncoding() == null ? "UTF8" : node.getOwnerDocument().getXmlEncoding());
	}
}
