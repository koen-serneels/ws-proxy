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
