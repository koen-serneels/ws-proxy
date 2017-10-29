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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.AbstractResource;

/**
 * Internal representation of the meta-data required to use a JKS. A keystore has at least a corresponding file on the
 * filesystem and a password to access the keystore.
 * 
 * @author Koen Serneels
 */
public class Keystore {

	private final AbstractResource keystore;
	private final String keystorePassword;
	private List<Pair<String, String>> keyAliasPasswords = new ArrayList<>();

	/**
	 * Use this if the keystore also contains keys. Left side is the alias, right side is the key password. The key
	 * password can be the same as that of the keystore itself.
	 */
	@SafeVarargs
	public Keystore(AbstractResource keystore, String keystorePassword, Pair<String, String>... keyAliasPasswords) {
		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
		this.keyAliasPasswords = Arrays.asList(keyAliasPasswords);
	}

	/**
	 * Use this if the keystore contains only certificates
	 */
	public Keystore(AbstractResource keystore, String keystorePassword) {
		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
	}

	public AbstractResource getKeystore() {
		return keystore;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public List<Pair<String, String>> getKeyAliasPasswords() {
		return keyAliasPasswords;
	}
}
