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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ForwardingClientSupportTest {

	public void testParseForwardProxyUrl() {
		ForwardingClientConfigurationSupport forwardingClientSupport = new ForwardingClientConfigurationSupport();

		Map<QName, Pair<String, Integer>> result = forwardingClientSupport.parseForwardProxyUrl("");
		Assert.assertTrue(result.isEmpty());

		try {
			result = forwardingClientSupport.parseForwardProxyUrl("service=localhost:8080");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("identifier must be in the form of: '{namespace}localpart"));
		}

		result = forwardingClientSupport.parseForwardProxyUrl("{http://dummy}service1=localhost:8080");

		Assert.assertEquals(result.get(new QName("http://dummy", "service1")).getLeft(), "localhost");
		Assert.assertEquals(result.get(new QName("http://dummy", "service1")).getRight(), new Integer(8080));

		result = forwardingClientSupport
				.parseForwardProxyUrl("{http://dummy}service1=localhost:8080,{http://dummy}service2=localhost:9090");

		Assert.assertEquals(result.get(new QName("http://dummy", "service2")).getLeft(), "localhost");
		Assert.assertEquals(result.get(new QName("http://dummy", "service2")).getRight(), new Integer(9090));

	}

	public void testParseEndpointTargetUrlMapping() {
		ForwardingClientConfigurationSupport forwardingClientSupport = new ForwardingClientConfigurationSupport();

		Map<QName, String> result = forwardingClientSupport.parseEndpointTargetUrlMapping("");
		Assert.assertTrue(result.isEmpty());

		try {
			result = forwardingClientSupport.parseEndpointTargetUrlMapping("service=localhost:8080/abc");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("identifier must be in the form of: '{namespace}localpart"));
		}

		result = forwardingClientSupport.parseEndpointTargetUrlMapping("{http://dummy}service1=localhost:8080/abc");

		Assert.assertEquals(result.get(new QName("http://dummy", "service1")), "localhost:8080/abc");

		result = forwardingClientSupport
				.parseEndpointTargetUrlMapping("{http://dummy}service1=localhost:8080/abc,{http://dummy}service2=localhost:8080/xyz");

		Assert.assertEquals(result.get(new QName("http://dummy", "service1")), "localhost:8080/abc");
		Assert.assertEquals(result.get(new QName("http://dummy", "service2")), "localhost:8080/xyz");
	}

	public void testSetServiceEndpointProtocolMapping() {
		ForwardingClientConfigurationSupport forwardingClientSupport = new ForwardingClientConfigurationSupport();

		Map<QName, String> result = forwardingClientSupport.parseEndpointProtocolMappings("");
		Assert.assertTrue(result.isEmpty());

		try {
			result = forwardingClientSupport.parseEndpointProtocolMappings("service=protocol");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("identifier must be in the form of: '{namespace}localpart"));
		}

		try {
			result = forwardingClientSupport.parseEndpointProtocolMappings("ns:service=proto=col");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("must contain configuration in the form of"));
		}

		result = forwardingClientSupport
				.parseEndpointProtocolMappings("{http://dummy}service1=HTTP,{http://dummy}service2=HtTpS");
		Assert.assertEquals(result.get(new QName("http://dummy", "service1")), "http");
		Assert.assertEquals(result.get(new QName("http://dummy", "service2")), "https");

		result = forwardingClientSupport.parseEndpointProtocolMappings("{http://dummy}service1=http");
		Assert.assertEquals(result.get(new QName("http://dummy", "service1")), "http");
	}
}
