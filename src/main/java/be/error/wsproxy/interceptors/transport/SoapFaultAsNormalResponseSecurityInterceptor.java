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
package be.error.wsproxy.interceptors.transport;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;

/**
 * Use this interceptor over {@link Wss4jSecurityInterceptor} when SOAP faults are/need also message security
 * 
 * @author Koen Serneels
 */
public class SoapFaultAsNormalResponseSecurityInterceptor extends Wss4jSecurityInterceptor {

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return super.handleResponse(messageContext, endpoint);
	}

	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		return super.handleResponse(messageContext);
	}
}
