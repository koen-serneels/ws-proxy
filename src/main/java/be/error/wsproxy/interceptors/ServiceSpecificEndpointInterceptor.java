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
package be.error.wsproxy.interceptors;

import org.springframework.ws.server.EndpointInterceptor;

/**
 * This is a marker interface for identifying interceptors for specific service purposes. For example: logging of
 * service specific data. Other interceptors, that are generic/system should directly implement
 * {@link EndpointInterceptor}.
 * <p>
 * Service specific interceptors implementing this interface will automatically be recognized by
 * {@link DefaultInterceptorExecutor}, which apply certain default interception logic.
 * 
 * @see DefaultInterceptorExecutor
 * @author Koen Serneels
 */
public interface ServiceSpecificEndpointInterceptor extends EndpointInterceptor {

}
