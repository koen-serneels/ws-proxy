package be.error.wsproxy.interceptors.logging;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Exception thrown when a mandatory xpath value was not present.
 * 
 * @author Koen Serneels
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class MandatoryXPathValueAbsentException extends RuntimeException {

	public MandatoryXPathValueAbsentException(String message) {
		super(message);
	}
}
