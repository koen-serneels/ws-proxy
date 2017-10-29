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
package be.error.wsproxy.configuration.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.transport.http.WebServiceMessageReceiverHandlerAdapter;

import be.error.wsproxy.core.CatchAllEndpoint;
import be.error.wsproxy.core.ProxySoapFaultMappingExceptionResolver;

/**
 * @author Koen Serneels
 */
@Configuration
@ComponentScan(basePackages = { "be.error.wsproxy" })
public class WsProxy {

	@Autowired
	private CatchAllEndpoint catchAllEndpoint;

	@Bean
	public SoapMessageFactory messageFactory() {
		return new SaajSoapMessageFactory();
	}

	@Bean
	public SoapMessageDispatcher messageDispatcher() {
		SoapMessageDispatcher soapMessageDispatcher = new SoapMessageDispatcher();
		List<EndpointMapping> endpointMappings = new ArrayList<>();
		endpointMappings.add(catchAllEndpointMapping());
		soapMessageDispatcher.setEndpointMappings(endpointMappings);
		return soapMessageDispatcher;
	}

	@Bean
	public PayloadRootAnnotationMethodEndpointMapping catchAllEndpointMapping() {
		PayloadRootAnnotationMethodEndpointMapping payloadRootQNameEndpointMapping = new PayloadRootAnnotationMethodEndpointMapping();
		payloadRootQNameEndpointMapping.setDefaultEndpoint(catchAllEndpoint);
		return payloadRootQNameEndpointMapping;
	}

	/**
	 * Correctly deal with the fault actor in SOAP faults
	 */
	@Bean
	public ProxySoapFaultMappingExceptionResolver proxySoapFaultMappingExceptionResolver() {
		ProxySoapFaultMappingExceptionResolver proxySoapFaultMappingExceptionResolver = new ProxySoapFaultMappingExceptionResolver(
				"be.error");
		SoapFaultDefinition soapFaultDefinition = new SoapFaultDefinition();
		soapFaultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
		proxySoapFaultMappingExceptionResolver.setDefaultFault(soapFaultDefinition);
		return proxySoapFaultMappingExceptionResolver;
	}

	/**
	 * Support for @SoapFault annotations on exceptions
	 */
	@Bean
	public SoapFaultAnnotationExceptionResolver soapFaultAnnotationExceptionResolver() {
		return new SoapFaultAnnotationExceptionResolver();
	}

	/**
	 * Configure the SpringMVC DispatcherServlet to be able to handle Web Service requests directly
	 */
	@Bean
	public WebServiceMessageReceiverHandlerAdapter webServiceMessageReceiverHandlerAdapter() {
		WebServiceMessageReceiverHandlerAdapter webServiceMessageReceiverHandlerAdapter = new WebServiceMessageReceiverHandlerAdapter();
		webServiceMessageReceiverHandlerAdapter.setMessageFactory(messageFactory());
		return webServiceMessageReceiverHandlerAdapter;

	}

	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
		simpleUrlHandlerMapping.setDefaultHandler(messageDispatcher());
		return simpleUrlHandlerMapping;
	}
}
