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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpComponentsConnection;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import be.error.wsproxy.configuration.properties.EndpointTargetUrlMapping;
import be.error.wsproxy.interceptors.ServiceSpecificEndpointInterceptor;
import be.error.wsproxy.interceptors.transport.HttpRequestHeaderTransfererInterceptor;

/**
 * Forwards the given {@link WebServiceMessage} to a target service and returns the response as
 * {@link WebServiceMessage}. Messages can both be forwarded to external services (from internal clients) or to internal
 * service (from external clients). In the former case that case we speak of internal inbound and external outbound.
 * Messages are received from internal clients and forward to external services. The former is for internally hosted
 * services, we speak from external inbound and internal outbound. Messages are received from external clients and
 * forwarded to our internally hosted services.<br>
 * <p>
 * 
 * The ForwardingClient will detect in which mode the module is operating for the current request in order to calculate
 * the target host. This happens according to following sequence:
 * 
 * <ul>
 * <li>Check if there is a {@link #setEndpointTargetUrlMapping(String)} set for the given target service. If such
 * configuration is present, the configured URL will be used as target URL. This can be an internal URL for a service
 * that is hosted internally, or an external URL of a service hosted externally. See also
 * {@link EndpointTargetUrlMapping}</li>
 * <li>If no reverse EndpointTargetUrlMapping exists for the given service, the target hostname (and port) of the
 * service to invoke is tried to be obtained from the "Host" HTTP header in the incoming request. This assumes that the
 * (internal) client configured the URL to this module as a forward HTTP proxy. This is the desired setup for accessing
 * externally hosted services as the configuration of the target URL does not need to be present on this module, but is
 * maintained by the client.
 * <ul>
 * <li>The target request path is obtained from the source request path minus the context root under which this module
 * is deployed. For example, suppose this module is deployed under the context root "ws-proxy". The client configures
 * the host and port of the appserver on which this modules runs as forward proxy. As target URL the client has to use
 * the original URL under which the target service is accessible. However, the client needs to prefix the original path
 * with the context root under which this module is deployed. so for example, if the original URL was
 * "http://serviceHost:3000/a/b/c" the client will have to use URL:
 * 'http://serviceHost:3000/contextRootOfThisModule/a/b/c', the target Webservice path to which this module will forward
 * will be:'/a/b/c'. If you want to avoid this, make sure this module is deployed directly deployed under the root (/),
 * nothing is (and needs to be) changed to the path the client uses.</li>
 * </ul>
 * </li>
 * </ul>
 * <b>Note:</b> when the client uses this module as a forward proxy, the client has to use the protocol that is support
 * by the application server on which the ws-proxy module runs. Normally this will be plain http for internal
 * communication. However, it might be that the target service requires https. In this case there are two options: use
 * {@link #setEndpointTargetUrlMapping(String)} to indicate for which target service this module has to use https to
 * forward the message to (http is default). Alternatively, you can setup an external reverse proxy and override the
 * target URL by using {@link #setEndpointTargetUrlMapping(String)} this might be a catch-all reverse proxy, you could
 * let every request for all external services accessed be forward to the reverse proxy. The actual target URL and
 * protocol will then be configured on the reverse proxy itself. If you don't use the forward proxy paradigm and you
 * configure each endpoint on this module (by using {@link #setEndpointTargetUrlMapping(String)}) you don't have to use
 * {@link #setServiceEndpointProtocolMapping(String)} since the protocol is part of the URL as configured via
 * {@link #setEndpointTargetUrlMapping(String)}<br>
 * <p>
 * 
 * If the source request contains a 'Authorization' header or a 'SOAPAction' header, those are standard added in the
 * HTTP request to the target webservice. {@link HttpRequestHeaderTransfererInterceptor}. Note: the 'SOAPAction' header
 * is added via the {@link SoapActionCallback} and directly by manipulating the request headers.<br>
 * <p>
 * 
 * In case the upstream service returns a SOAPFault, the message is set on the response as any other (valid) response
 * message. In that case the fault handling by configured interceptors (if any) will be triggered. This will work for
 * interceptors defined here (the 'to external' chain) as well as interceptors defined on the 'from internal' chain on
 * the {@link CatchAllEndpoint}. For example the
 * {@link ServiceSpecificEndpointInterceptor#handleFault(MessageContext, Object)} method is invoked instead of
 * {@link ServiceSpecificEndpointInterceptor#handleResponse(MessageContext, Object)}.<br>
 * <p>
 * 
 * When the target Webservice needs to be reached via a forward proxy, you can set its hostname:port (hostname alone
 * will assume port 80 by default) via {@link #setHttpForwardProxy(String)}. A forward proxy is transparant. The target
 * URL to which the request will be forwarded is the same as when no proxy was configured at all (it is calculated from
 * the "Host" HTTP header and original request path, as explained above). The forward proxy information is set on the
 * HTTP client stack as HTTP proxy. If you need TLS for forwarding you can identify the webservices for which this
 * should be done using ({@link #setServiceEndpointProtocolMapping}. <br>
 * 
 * @see EndpointTargetUrlMapping
 * @author Koen Serneels
 */

@Component
public class ForwardingClient {

	public static final String SOAP_ACTION_HEADER = "SOAPAction";

	private static final Logger LOG = Logger.getLogger(ForwardingClient.class);

	private static final String DEFAULT_TARGET_PROTOCOL = "http";
	private static final String HOST_HEADER = "Host";

	private final ForwardingClientConfiguration config = new ForwardingClientConfiguration();
	private final WebServiceMessageFactory webServiceMessageFactory;

	@Autowired
	public ForwardingClient(WebServiceMessageFactory webServiceMessageFactory) {
		this.webServiceMessageFactory = webServiceMessageFactory;
	}

	public WebServiceMessage forward(final QName webServiceIdentifier, final WebServiceMessage webServiceMessage,
			final HttpServletRequest httpServletRequest) {
		final SaajSoapMessage responseMessage = (SaajSoapMessage) webServiceMessageFactory.createWebServiceMessage();
		String targetUrl = calculateTargetUrl(webServiceIdentifier, httpServletRequest);
		WebServiceTemplate webServiceTemplate = createAndConfigureWebServiceTemplate(responseMessage,
				webServiceIdentifier, httpServletRequest);

		webServiceTemplate.sendAndReceive(targetUrl, new WebServiceMessageCallback() {
			@Override
			public void doWithMessage(WebServiceMessage message) throws IOException {
				SaajSoapMessage saajSoapMessage = ((SaajSoapMessage) message);
				saajSoapMessage.setSaajMessage(((SaajSoapMessage) webServiceMessage).getSaajMessage());
				new SoapActionCallback(httpServletRequest.getHeader(SOAP_ACTION_HEADER)).doWithMessage(message);
			}
		}, new WebServiceMessageCallback() {
			@Override
			public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
				try {
					responseMessage.getSaajMessage().getSOAPPart()
							.setContent(((SaajSoapMessage) message).getEnvelope().getSource());
				} catch (SOAPException soapException) {
					throw new RuntimeException(soapException);
				}
			}
		});

		LOG.debug("Forwarding (" + targetUrl + ") done.");
		return responseMessage;
	}

	private WebServiceTemplate createAndConfigureWebServiceTemplate(final SaajSoapMessage responseMessage,
			QName webServiceIdentifier, HttpServletRequest httpServletRequest) {
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate(webServiceMessageFactory) {
			@Override
			protected Object handleError(WebServiceConnection connection, WebServiceMessage request) throws IOException {
				HttpResponse httpResponse = ((HttpComponentsConnection) connection).getHttpResponse();
				String statusAndCode = httpResponse.getStatusLine().getReasonPhrase() + " - "
						+ httpResponse.getStatusLine().getStatusCode();
				Charset charset = httpResponse.getEntity().getContentEncoding() != null
						&& httpResponse.getEntity().getContentEncoding().getValue() != null ? Charset
						.forName(httpResponse.getEntity().getContentEncoding().getValue()) : Charset.defaultCharset();
				String response = httpResponse.getEntity() != null ? StreamUtils.copyToString(httpResponse.getEntity()
						.getContent(), charset) : "N/A";
				throw new WebServiceTransportException("[Status: " + statusAndCode + "] [Reponse " + response + "]");
			}
		};
		webServiceTemplate.setMessageSender(createAndInitWebServiceMessageSender(webServiceIdentifier));
		// In case the target endpoint returns a clean SoapFault, the normal response callback is abandoned and the
		// FaulMessageResolver is invoked. In this case we want to return the fault unaltered to the client, so we set
		// it here as the response.
		webServiceTemplate.setFaultMessageResolver(new FaultMessageResolver() {
			@Override
			public void resolveFault(WebServiceMessage message) throws IOException {
				responseMessage.setSaajMessage(((SaajSoapMessage) message).getSaajMessage());
			}
		});

		List<ClientInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HttpRequestHeaderTransfererInterceptor(HttpRequestHeaderTransfererInterceptor
				.extractHeaders(httpServletRequest)));

		if (config.getCustomClientInterceptors(webServiceIdentifier) != null) {
			interceptors.addAll(config.getCustomClientInterceptors(webServiceIdentifier));
		}

		LOG.debug("Using interceptors:" + Arrays.toString(ClassUtils.toClass(interceptors.toArray())));

		webServiceTemplate.setInterceptors(interceptors.toArray(new ClientInterceptor[0]));
		return webServiceTemplate;
	}

	@SuppressWarnings("deprecation")
	private HttpComponentsMessageSender createAndInitWebServiceMessageSender(QName webServiceIdentifier) {
		HttpComponentsMessageSender webServiceMessageSender = new HttpComponentsMessageSender();
		webServiceMessageSender.setConnectionTimeout(30000); // 30 seconds connection timeout
		webServiceMessageSender.setReadTimeout(120000); // 2 minutes read timeout on socket

		if (config.getHttpForwardProxy(webServiceIdentifier) != null) {
			Pair<String, Integer> forwardProxy = config.getHttpForwardProxy(webServiceIdentifier);
			webServiceMessageSender
					.getHttpClient()
					.getParams()
					.setParameter(org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY,
							new HttpHost(forwardProxy.getLeft(), forwardProxy.getRight()));
		}
		return webServiceMessageSender;
	}

	private String calculateTargetUrl(QName webServiceIdentifier, HttpServletRequest httpServletRequest) {
		String targetUrl = null;

		if (StringUtils.isNotBlank(config.getEndpointTargetUrlMapping(webServiceIdentifier))) {
			targetUrl = config.getEndpointTargetUrlMapping(webServiceIdentifier);
			LOG.debug("Using EndpointTargetUrlMapping as base hostname/port");
		} else {
			String host = httpServletRequest.getHeader(HOST_HEADER);
			if (StringUtils.isBlank(host)) {
				throw new IllegalArgumentException(
						"No target host could be calculated. No EndpointTargetUrlMapping present and no Host header information found for service: "
								+ webServiceIdentifier);
			}
			LOG.debug("Using information from Host header as hostname/port");
			String contextPath = (StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ? httpServletRequest
					.getRequestURI().replaceFirst(httpServletRequest.getContextPath(), "") : httpServletRequest
					.getRequestURI());
			String protocol = StringUtils.isBlank(config.getEndpointProtocolMapping(webServiceIdentifier)) ? DEFAULT_TARGET_PROTOCOL
					: config.getEndpointProtocolMapping(webServiceIdentifier);
			targetUrl = protocol + "://" + host + contextPath;
		}

		LOG.debug("Got webservice forwarding request, sending to:" + targetUrl);
		return targetUrl;
	}

	// Properties

	@Resource
	public void setHttpForwardProxy(String httpForwardProxy) {
		if (StringUtils.isNotEmpty(StringUtils.trimToNull(httpForwardProxy))) {
			config.setHttpForwardProxy(httpForwardProxy);
		}
	}

	@Resource
	public void setEndpointTargetUrlMapping(String endpointTargetUrlMapping) {
		if (StringUtils.isNotEmpty(StringUtils.trimToNull(endpointTargetUrlMapping))) {
			config.setEndpointTargetUrlMapping(endpointTargetUrlMapping);
		}
	}

	@Resource
	public void setServiceEndpointProtocolMapping(String serviceEndpointProtocolMapping) {
		if (StringUtils.isNotBlank(serviceEndpointProtocolMapping)) {
			config.setEndpointProtocolMapping(serviceEndpointProtocolMapping);
		}
	}

	@Resource
	public void setCustomClientInterceptors(Map<QName, List<ClientInterceptor>> customClientInterceptors) {
		config.setCustomClientInterceptors(customClientInterceptors);
	}

	ForwardingClientConfiguration getForwardingClientConfiguration() {
		return config;
	}
}
