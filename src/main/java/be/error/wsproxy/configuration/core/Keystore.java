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
