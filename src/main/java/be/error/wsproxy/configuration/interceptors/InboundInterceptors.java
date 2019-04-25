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
package be.error.wsproxy.configuration.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.server.endpoint.interceptor.DelegatingSmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;

import be.error.wsproxy.interceptors.logging.LoggingInterceptor;
import be.error.wsproxy.interceptors.logging.LoggingXPathInterceptor;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData;

/**
 * Interceptors for the internal chain are configured in three layers:
 * 
 * <ul>
 * <li>First in line interceptors</li>
 * <li>Service specific interceptors</li>
 * <li>Last in line interceptors</li>
 * </ul>
 * 
 * The first and last in line interceptors are meant to be generic; they are service agnostic. The service specific
 * interceptors are configured for specific services, based on soap action or the payload root element.
 * 
 */
@Configuration
public class InboundInterceptors {

	@Autowired
	private PayloadRootAnnotationMethodEndpointMapping catchAllEndpointMapping;
	@Autowired
	private MessageDispatcher messageDispatcher;

	@Configuration
	public static class FirstInlineInterceptors {
		@Bean
		public DelegatingSmartSoapEndpointInterceptor loggingInterceptor() {
			return new DelegatingSmartSoapEndpointInterceptor(new LoggingInterceptor());
		}
	}

	@Configuration
	public static class ServiceSpecificInterceptors {
		@Bean
		public PayloadRootSmartSoapEndpointInterceptor getCurrentDateLoggingInterceptor() {
			LoggingXPathInterceptor loggingXPathInterceptor = new LoggingXPathInterceptor();
			loggingXPathInterceptor.addRequestXPaths(new WebServiceMessageXPathExpressionMetaData(
					"//*[local-name()='arg0']", "requestParameter"));
			loggingXPathInterceptor.addResponseXPaths(new WebServiceMessageXPathExpressionMetaData(
					"//*[local-name()='return']", "responseParameter"));
			return new PayloadRootSmartSoapEndpointInterceptor(loggingXPathInterceptor, "http://wsproxy.error.be/",
					"getCurrentDate");
		}
	}

	@Configuration
	public static class LastInLineInterceptors {

	}
}
