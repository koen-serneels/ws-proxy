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

/**
 * {@link RuntimeException} thrown by {@link CatchAllEndpoint} when there was a problem communicating with the upstream
 * service. This is a general error containing *any* problem that might arise when communicating with the upstream
 * service. This exception exists so problems from/with the target service can be clearly identified from other internal
 * problems of this module.
 * 
 * @see CatchAllEndpoint
 * @see ProxySoapFaultMappingExceptionResolver
 * @author Koen Serneels
 */
public class ForwardingEndpointTargetException extends RuntimeException {

	public ForwardingEndpointTargetException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
