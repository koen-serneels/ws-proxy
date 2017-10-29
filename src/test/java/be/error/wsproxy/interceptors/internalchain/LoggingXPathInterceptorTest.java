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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.mockito.Mockito;
import org.springframework.ws.context.MessageContext;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import be.error.wsproxy.interceptors.logging.LoggingXPathInterceptor;
import be.error.wsproxy.interceptors.logging.MandatoryXPathValueAbsentException;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionResultCardinality;

@Test
public class LoggingXPathInterceptorTest {
	private final String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:common=\"http://common.error.be\" xmlns:service="
			+ "\"http://service.error.be\"><soapenv:Header><common:OwnOtherElement>ownOtherBogusValue</common:OwnOtherElement><common:SomeId>12345</common:SomeId><SomeElement>"
			+ "bogusValue</SomeElement></soapenv:Header><soapenv:Body><service:SomeService><FirstElement><FirstElementFirstChild>val</FirstElementFirstChild></FirstElement>"
			+ "<SecondElement>123</SecondElement></service:SomeService></soapenv:Body></soapenv:Envelope>";

	private LoggingXPathInterceptor xpathLoggedActivityInterceptor;
	private MessageContext messageContext;
	private final DummyAppender dummyAppender = new DummyAppender();

	@BeforeTest
	public void initAppender() {
		Logger logger = Logger.getLogger(LoggingXPathInterceptor.class);
		logger.setLevel(Level.DEBUG);
		logger.addAppender(dummyAppender);
	}

	@AfterTest
	public void removeAppender() {
		// logger.removeAppender(dummyAppender);
	}

	@BeforeMethod
	public void setup() {
		dummyAppender.clearEvents();
		messageContext = Mockito.mock(MessageContext.class);
		Mockito.when(messageContext.getRequest()).thenReturn(WebserviceTestSupport.createSoapWebServiceMessage(xml));
		Mockito.when(messageContext.getResponse()).thenReturn(WebserviceTestSupport.createSoapWebServiceMessage(xml));
		xpathLoggedActivityInterceptor = new LoggingXPathInterceptor();
	}

	/**
	 * In this scenario we expect that one logging is made of level debug. The message should contain the information
	 * requested by the xpaths we used for the request and response.
	 */
	public void testAllGoodScenario() throws Exception {
		xpathLoggedActivityInterceptor.addRequestXPaths(new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='SomeService']/*[local-name()='SecondElement']", "someKey"));

		xpathLoggedActivityInterceptor.handleRequest(messageContext, null);
		xpathLoggedActivityInterceptor.handleResponse(messageContext, null);

		Assert.assertEquals(dummyAppender.getEvents().size(), 1);

		LoggingEvent loggingEvent = dummyAppender.getEvents().iterator().next();
		Assert.assertEquals(loggingEvent.getLevel(), Level.DEBUG);
		Assert.assertEquals(loggingEvent.getMessage().toString(),
				"SID:{http://service.error.be}SomeService XPATHID:someKey VALUE:123");
	}

	/**
	 * In this scenario we expect that one logging is made of level debug and one of warning. The warning originates in
	 * not finding the requested xpath which has a mandatory but non blocking nature.
	 */
	public void testParameterWarningScenario() throws Exception {
		WebServiceMessageXPathExpressionMetaData webServiceMessageXPathExpressionMetaData = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='SomeService']/*[local-name()='ThisDoesNotExist']", "someKey");
		xpathLoggedActivityInterceptor.addRequestXPaths(webServiceMessageXPathExpressionMetaData);

		xpathLoggedActivityInterceptor.handleRequest(messageContext, null);
		xpathLoggedActivityInterceptor.handleResponse(messageContext, null);

		Assert.assertEquals(dummyAppender.getEvents().size(), 1);

		LoggingEvent loggingEvent = dummyAppender.getEvents().iterator().next();
		Assert.assertEquals(loggingEvent.getLevel(), Level.DEBUG);
		Assert.assertEquals(loggingEvent.getMessage().toString(),
				"SID:{http://service.error.be}SomeService XPATHID:someKey VALUE:");
	}

	/**
	 * In this scenario we expect that one logging is made of level error. The error originates in not finding the
	 * requested xpath which has a mandatory blocking nature.
	 */
	public void testParameterErrorRequestScenario() throws Exception {
		WebServiceMessageXPathExpressionMetaData webServiceMessageXPathExpressionMetaData = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='SomeService']/*[local-name()='ThisDoesNotExist']", "someKey");
		webServiceMessageXPathExpressionMetaData
				.setXPathExpressionResultCardinality(XPathExpressionResultCardinality.MANDATORY_BLOCKING);
		xpathLoggedActivityInterceptor.addRequestXPaths(webServiceMessageXPathExpressionMetaData);

		try {
			xpathLoggedActivityInterceptor.handleRequest(messageContext, null);
		} catch (MandatoryXPathValueAbsentException mandatoryXpathValueAbsentException) {
			// We need to manually call handleFault (which is normally done by the framework)
			xpathLoggedActivityInterceptor.handleFault(messageContext, null);
		}

		Assert.assertEquals(dummyAppender.getEvents().size(), 1);

		LoggingEvent loggingEvent = dummyAppender.getEvents().iterator().next();
		Assert.assertEquals(loggingEvent.getLevel(), Level.ERROR);
		Assert.assertEquals(
				loggingEvent.getMessage().toString(),
				"SID:{http://service.error.be}SomeService Payload:<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
						+ "<service:SomeService xmlns:service=\"http://service.error.be\"><FirstElement><FirstElementFirstChild>"
						+ "val</FirstElementFirstChild></FirstElement><SecondElement>123</SecondElement></service:SomeService>");
	}

	/**
	 * In this scenario we expect that two logging are made. One of level debug which extracts the request xpath
	 * normally from the request and one of error because the requested xpath is not present in the reponse but marked
	 * as mandatory blocking.
	 */
	public void testParameterErrorReponseScenario() throws Exception {
		WebServiceMessageXPathExpressionMetaData webServiceMessageXPathExpressionMetaData = new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='SomeService']/*[local-name()='ThisDoesNotExist']", "someKey");
		webServiceMessageXPathExpressionMetaData
				.setXPathExpressionResultCardinality(XPathExpressionResultCardinality.MANDATORY_BLOCKING);

		xpathLoggedActivityInterceptor.addResponseXPaths(webServiceMessageXPathExpressionMetaData);
		xpathLoggedActivityInterceptor.addRequestXPaths(new WebServiceMessageXPathExpressionMetaData(
				"//*[local-name()='SomeService']/*[local-name()='SecondElement']", "someKey"));

		xpathLoggedActivityInterceptor.handleRequest(messageContext, null);

		try {
			xpathLoggedActivityInterceptor.handleResponse(messageContext, null);
		} catch (MandatoryXPathValueAbsentException mandatoryXpathValueAbsentException) {
			// We need to manually call handleFault (which is normally done by the framework)
			xpathLoggedActivityInterceptor.handleFault(messageContext, null);
		}

		Assert.assertEquals(dummyAppender.getEvents().size(), 2);

		LoggingEvent loggingEvent = dummyAppender.getEvents().get(0);
		Assert.assertEquals(loggingEvent.getLevel(), Level.DEBUG);
		Assert.assertEquals(loggingEvent.getMessage().toString(),
				"SID:{http://service.error.be}SomeService XPATHID:someKey VALUE:123");

		loggingEvent = dummyAppender.getEvents().get(1);
		Assert.assertEquals(loggingEvent.getLevel(), Level.ERROR);
		Assert.assertEquals(
				loggingEvent.getMessage().toString(),
				"SID:{http://service.error.be}SomeService Payload:<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
						+ "<service:SomeService xmlns:service=\"http://service.error.be\"><FirstElement><FirstElementFirstChild>"
						+ "val</FirstElementFirstChild></FirstElement><SecondElement>123</SecondElement></service:SomeService>");
	}

	private static class DummyAppender extends AppenderSkeleton {

		private final List<LoggingEvent> events = new ArrayList<>();

		@Override
		public void close() {
			// Do nothing
		}

		@Override
		protected void append(LoggingEvent event) {
			events.add(event);
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		public void clearEvents() {
			events.clear();
		}

		public List<LoggingEvent> getEvents() {
			return events;
		}
	}
}
