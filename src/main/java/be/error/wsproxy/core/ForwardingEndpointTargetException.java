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
