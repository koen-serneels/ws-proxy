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
package be.error.wsproxy.interceptors.logging;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.springframework.util.xml.TransformerUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerHelper;

import be.error.wsproxy.core.WebserviceIdentifierExtractorSupport;

public class LoggingInterceptor implements EndpointInterceptor, ClientInterceptor {

	private static final Logger LOG = Logger.getLogger(LoggingInterceptor.class);

	private final TransformerHelper transformerHelper = new TransformerHelper();
	private final Transformer transformer;

	{
		try {
			transformer = transformerHelper.createTransformer();
			TransformerUtils.enableIndenting(transformer);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " OUTBOUND SIDE Request:" + transform(messageContext.getRequest()));
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " OUTBOUND SIDE Response:" + transform(messageContext.getResponse()));
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " OUTBOUND SIDE Fault:" + transform(messageContext.getResponse()));
		return true;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " INBOUND SIDE Request:" + transform(messageContext.getRequest()));
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " INBOUND SIDE Response:" + transform(messageContext.getResponse()));
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
				+ " INBOUND SIDE Fault:" + transform(messageContext.getResponse()));
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
		// Do nothing
	}

	@Override
	public void afterCompletion(final MessageContext messageContext, final Exception ex) throws WebServiceClientException {
		//Do nothing
	}

	private String transform(WebServiceMessage message) {
		SaajSoapMessage saajSoapMessage = ((SaajSoapMessage) message);
		StringResult stringResult = new StringResult();
		try {
			transformer.transform(new DOMSource(saajSoapMessage.getDocument()), stringResult);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
		return stringResult.toString();
	}
}
