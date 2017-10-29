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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;

import be.error.wsproxy.core.MessageSupport;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionResultCardinality;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionScope;

/**
 * Helper class for executing XPath expressions (via {@link WebServiceMessageXPathExpressionMetaData} on a
 * {@link WebServiceMessage}. The result is a map which has as key the XPath identifier contained by
 * {@link WebServiceMessageXPathExpressionMetaData#getXPathKey()}. The value is the actual value as selected by the
 * XPath expression: {@link WebServiceMessageXPathExpressionMetaData#getXPathExpression()} (can be null or empty string
 * if no value was found or the path does not refer to an existing node).
 * 
 * @see LoggingXPathInterceptor
 * 
 * @author Koen Serneels
 */
public class WebServiceMessageXPathExtractor {

	private static final Logger LOG = Logger.getLogger(WebServiceMessageXPathExtractor.class);

	public Map<WebServiceMessageXPathExpressionMetaData, String> extractXPath(
			WebServiceMessageXPathExpressionMetaData webServiceMessageIdentifiableXPathExpression,
			WebServiceMessage webServiceMessage) throws TransformerConfigurationException, TransformerException,
			UnsupportedEncodingException {
		List<WebServiceMessageXPathExpressionMetaData> list = new ArrayList<>();
		list.add(webServiceMessageIdentifiableXPathExpression);
		return extractXPaths(list, webServiceMessage);
	}

	public Map<WebServiceMessageXPathExpressionMetaData, String> extractXPaths(
			List<WebServiceMessageXPathExpressionMetaData> webServiceMessageIdentifiableXPathExpressions,
			WebServiceMessage webServiceMessage) throws TransformerConfigurationException, TransformerException,
			UnsupportedEncodingException {
		Map<WebServiceMessageXPathExpressionMetaData, String> xpathResults = new HashMap<>();
		for (WebServiceMessageXPathExpressionMetaData webServiceMessageIdentifiableXPathExpression : webServiceMessageIdentifiableXPathExpressions) {
			Node messageNode;
			if (webServiceMessageIdentifiableXPathExpression.getXPathExpressionScope() == XPathExpressionScope.SOAP_BODY) {
				messageNode = MessageSupport.transformSourceToNode(webServiceMessage.getPayloadSource());
			} else {
				messageNode = MessageSupport.transformSourceToNode(((SoapMessage) webServiceMessage).getEnvelope()
						.getSource());
			}

			switch (webServiceMessageIdentifiableXPathExpression.getXPathExpressionType()) {
			case NODE:
				xpathResults.put(webServiceMessageIdentifiableXPathExpression, MessageSupport
						.transformNodeToString(webServiceMessageIdentifiableXPathExpression.getXPathExpression()
								.evaluateAsNode(messageNode)));
				break;
			case VALUE:
				xpathResults
						.put(webServiceMessageIdentifiableXPathExpression, webServiceMessageIdentifiableXPathExpression
								.getXPathExpression().evaluateAsString(messageNode));
				break;
			}
		}
		return validate(xpathResults);
	}

	private Map<WebServiceMessageXPathExpressionMetaData, String> validate(
			Map<WebServiceMessageXPathExpressionMetaData, String> extractedFragments) {
		List<String> mandatoryButMissingFragments = new ArrayList<>();

		for (Entry<WebServiceMessageXPathExpressionMetaData, String> entry : extractedFragments.entrySet()) {
			if (StringUtils.isEmpty(entry.getValue())) {
				if (entry.getKey().getXPathExpressionResultCardinality() == XPathExpressionResultCardinality.MANDATORY) {
					mandatoryButMissingFragments.add(entry.getKey().getXPathKey());
				} else if (entry.getKey().getXPathExpressionResultCardinality() == XPathExpressionResultCardinality.MANDATORY_BLOCKING) {
					MandatoryXPathValueAbsentException mandatoryXpathValueAbsentException = new MandatoryXPathValueAbsentException(
							"XPath value identified by expression: " + entry.getKey().getUnparsedXpathExpression()
									+ " returned no value but was indicated as mandatory and blocking.");
					LOG.error(mandatoryXpathValueAbsentException);
					throw mandatoryXpathValueAbsentException;
				}
			}
		}

		if (mandatoryButMissingFragments.size() > 0) {
			LOG.warn("One or more mandatory parameters could not be obtained from the message:"
					+ StringUtils.join(mandatoryButMissingFragments, ","));
		}

		return extractedFragments;
	}
}
