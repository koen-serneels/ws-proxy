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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.security.components.crypto.Crypto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean;

import be.error.wsproxy.configuration.core.Keystore;
import be.error.wsproxy.interceptors.logging.LoggingInterceptor;

@Configuration
public class OutboundInterceptors {

	@Resource
	private Keystore keystore;
	@Resource
	private Keystore truststore;

	@Resource
	private Crypto keystoreCrypto;
	@Resource
	private Crypto truststoreCrypto;

	@Bean
	public Map<QName, List<ClientInterceptor>> customClientInterceptors() throws Exception {
		Map<QName, List<ClientInterceptor>> mapping = new HashMap<>();

		List<ClientInterceptor> list = new ArrayList<>();
		list.add(getCurrentDateServiceSecurityInterceptor());
		list.add(new LoggingInterceptor());
		mapping.put(new QName("http://wsproxy.error.be/", "getCurrentDateSecured"), list);

		return mapping;
	}

	private Wss4jSecurityInterceptor getCurrentDateServiceSecurityInterceptor() throws Exception {
		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

		// Outgoing
		interceptor.setSecurementActions("Signature Timestamp");
		interceptor
				.setSecurementSignatureParts("{}{http://schemas.xmlsoap.org/soap/envelope/}Body;{}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp");
		interceptor.setSecurementSignatureKeyIdentifier("IssuerSerial");
		Pair<String, String> key = keystore.getKeyAliasPasswords().get(0);
		interceptor.setSecurementUsername(key.getLeft());
		interceptor.setSecurementPassword(key.getRight());
		interceptor.setSecurementSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
		interceptor.setSecurementSignatureDigestAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
		interceptor.setSecurementTimeToLive(700);
		interceptor.setValidationTimeToLive(700);
		interceptor.setSecurementSignatureCrypto(keystoreCrypto);

		// Incomming
		interceptor.setValidationActions("Timestamp Signature");
		interceptor.setValidationSignatureCrypto(truststoreCrypto);

		return interceptor;
	}

	@Bean
	public CryptoFactoryBean keystoreCrypto() throws Exception {
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setKeyStoreLocation(keystore.getKeystore());
		cryptoFactoryBean.setDefaultX509Alias("mykey");

		cryptoFactoryBean.setKeyStorePassword(keystore.getKeystorePassword());
		return cryptoFactoryBean;
	}

	@Bean
	public CryptoFactoryBean truststoreCrypto() throws Exception {
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setKeyStoreLocation(truststore.getKeystore());
		cryptoFactoryBean.setKeyStorePassword(truststore.getKeystorePassword());
		return cryptoFactoryBean;
	}
}
