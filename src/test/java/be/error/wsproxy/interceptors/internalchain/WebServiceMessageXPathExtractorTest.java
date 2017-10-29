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
package be.error.wsproxy.interceptors.internalchain;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExtractor;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionScope;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionType;

@Test
public class WebServiceMessageXPathExtractorTest {

	private final WebServiceMessageXPathExtractor xpathExtractor = new WebServiceMessageXPathExtractor();
	private final String soapEnvelopedXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:common=\"http://common.error.be\" xmlns:service="
			+ "\"http://service.error.be\"><soapenv:Header><common:OwnOtherElement>ownOtherBogusValue</common:OwnOtherElement><common:SomeId>12345</common:SomeId><SomeElement>"
			+ "bogusValue</SomeElement></soapenv:Header><soapenv:Body><service:SomeService><FirstElement><FirstElementFirstChild>val</FirstElementFirstChild></FirstElement>"
			+ "<SecondElement>123</SecondElement></service:SomeService></soapenv:Body></soapenv:Envelope>";

	public void testXpathExtractorExtractValues() throws Exception {

		Map<WebServiceMessageXPathExpressionMetaData, String> result = xpathExtractor.extractXPath(
				new WebServiceMessageXPathExpressionMetaData(
						"//*[local-name()='SomeService']/*[local-name()='SecondElement']", "someKey"),
				WebserviceTestSupport.createSoapWebServiceMessage(soapEnvelopedXml));
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.entrySet().iterator().next().getKey().getXPathKey(), "someKey");
		Assert.assertEquals(result.entrySet().iterator().next().getValue(), "123");

		WebServiceMessageXPathExpressionMetaData webServiceMessageIdentifiableXPathExpression = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='Envelope']/*[local-name()='Body']/*"
						+ "[local-name()='SomeService']/*[local-name()='FirstElement']/*[local-name()='FirstElementFirstChild']",
				"someKey");
		webServiceMessageIdentifiableXPathExpression.setXPathExpressionScope(XPathExpressionScope.SOAP_ENVELOPE);

		result = xpathExtractor.extractXPath(webServiceMessageIdentifiableXPathExpression,
				WebserviceTestSupport.createSoapWebServiceMessage(soapEnvelopedXml));
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.entrySet().iterator().next().getKey().getXPathKey(), "someKey");
		Assert.assertEquals(result.entrySet().iterator().next().getValue(), "val");

		webServiceMessageIdentifiableXPathExpression = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='SomeService']/*[local-name()='NonExistingElement']",
				"someKey");
		webServiceMessageIdentifiableXPathExpression.setXPathExpressionScope(XPathExpressionScope.SOAP_ENVELOPE);

		result = xpathExtractor.extractXPath(webServiceMessageIdentifiableXPathExpression,
				WebserviceTestSupport.createSoapWebServiceMessage(soapEnvelopedXml));

		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.entrySet().iterator().next().getKey().getXPathKey(), "someKey");
		Assert.assertTrue(StringUtils.isEmpty(result.entrySet().iterator().next().getValue()));

		webServiceMessageIdentifiableXPathExpression = new WebServiceMessageXPathExpressionMetaData(
				"*[local-name()='Envelope']/*[local-name()='Header']/*[local-name()='SomeId']", "someId");
		webServiceMessageIdentifiableXPathExpression.setXPathExpressionScope(XPathExpressionScope.SOAP_ENVELOPE);

		result = xpathExtractor.extractXPath(webServiceMessageIdentifiableXPathExpression,
				WebserviceTestSupport.createSoapWebServiceMessage(soapEnvelopedXml));

		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.entrySet().iterator().next().getKey().getXPathKey(), "someId");
		Assert.assertEquals(result.entrySet().iterator().next().getValue(), "12345");
	}

	public void testXpathExtractorExtractNode() throws Exception {

		WebServiceMessageXPathExpressionMetaData webServiceMessageIdentifiableXPathExpression = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='SomeService']", "someKey");
		webServiceMessageIdentifiableXPathExpression.setXPathExpressionScope(XPathExpressionScope.SOAP_ENVELOPE);
		webServiceMessageIdentifiableXPathExpression.setXPathExpressionType(XPathExpressionType.NODE);

		Map<WebServiceMessageXPathExpressionMetaData, String> result = xpathExtractor.extractXPath(
				webServiceMessageIdentifiableXPathExpression,
				WebserviceTestSupport.createSoapWebServiceMessage(soapEnvelopedXml));

		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.entrySet().iterator().next().getKey().getXPathKey(), "someKey");
		Assert.assertEquals(
				result.entrySet().iterator().next().getValue(),
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><service:SomeService xmlns:service=\"http://service.error.be\">"
						+ "<FirstElement><FirstElementFirstChild>val</FirstElementFirstChild></FirstElement><SecondElement>123"
						+ "</SecondElement></service:SomeService>");
	}
}
