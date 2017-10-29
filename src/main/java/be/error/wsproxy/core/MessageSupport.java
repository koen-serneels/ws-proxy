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
