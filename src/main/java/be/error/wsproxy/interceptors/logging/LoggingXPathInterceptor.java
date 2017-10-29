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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.ws.context.MessageContext;

import be.error.wsproxy.core.MessageSupport;
import be.error.wsproxy.core.WebserviceIdentifierExtractorSupport;
import be.error.wsproxy.interceptors.ServiceSpecificEndpointInterceptor;

/**
 * Service specific interceptor that will use XPath to extract information from the request/response and log it.
 * <p>
 * When this filter is enabled for a given WebService request, it extracts the XML parts identified by XPath expressions
 * ({@link WebServiceMessageXPathExpressionMetaData}) from request and response. This information is logged, in this
 * implementation by log4j.
 * 
 * @see #handleFault(MessageContext, Object)
 * @see #handleRequest(MessageContext, Object)
 * @see #handleResponse(MessageContext, Object)
 * 
 * @see WebServiceMessageXPathExpressionMetaData
 * @see WebServiceMessageXPathExtractor
 * 
 * @author Koen Serneels
 */
public class LoggingXPathInterceptor implements ServiceSpecificEndpointInterceptor {

	private static final Logger LOG = Logger.getLogger(LoggingXPathInterceptor.class);

	private final WebServiceMessageXPathExtractor xpathExtractor = new WebServiceMessageXPathExtractor();
	private final List<WebServiceMessageXPathExpressionMetaData> requestXPaths = new ArrayList<>();
	private final List<WebServiceMessageXPathExpressionMetaData> responseXPaths = new ArrayList<>();
	private boolean logSoapFaults = true;

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		logFragments(xpathExtractor.extractXPaths(requestXPaths, messageContext.getRequest()), messageContext);
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		logFragments(xpathExtractor.extractXPaths(responseXPaths, messageContext.getResponse()), messageContext);
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		if (logSoapFaults) {
			LOG.error("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
					+ " Payload:"
					+ MessageSupport.transformSourceToString(messageContext.getRequest().getPayloadSource()));
		}
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
		// Do nothing
	}

	private void logFragments(Map<WebServiceMessageXPathExpressionMetaData, String> extractedFragments,
			MessageContext messageContext) {
		for (Entry<WebServiceMessageXPathExpressionMetaData, String> entry : extractedFragments.entrySet()) {
			LOG.debug("SID:" + WebserviceIdentifierExtractorSupport.getWebServiceIdentifier(messageContext)
					+ " XPATHID:" + entry.getKey().getXPathKey() + " VALUE:" + entry.getValue());
		}
	}

	public void addResponseXPaths(WebServiceMessageXPathExpressionMetaData... responseXPaths) {
		if (responseXPaths != null) {
			this.responseXPaths.addAll(Arrays.asList(responseXPaths));
		}
	}

	public void addRequestXPaths(WebServiceMessageXPathExpressionMetaData... requestXPaths) {
		if (requestXPaths != null) {
			this.requestXPaths.addAll(Arrays.asList(requestXPaths));
		}
	}

	public void setLogSoapFaults(boolean logSoapFaults) {
		this.logSoapFaults = logSoapFaults;
	}
}
