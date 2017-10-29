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
package be.error.wsproxy.interceptors.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactoryBean;

/**
 * Contains an {@link XPathExpression} and extra data to execute the expression on a {@link WebServiceMessage}.
 * <ul>
 * <li>The type of result that is selected by the expression, either {@link XPathExpressionType#NODE} or
 * {@link XPathExpressionType#VALUE}.</li>
 * <li>The scope in which the XPath is expected to execute. Either from the start of the SOAP document
 * {@link XPathExpressionScope#SOAP_ENVELOPE} or from the body {@link XPathExpressionScope#SOAP_BODY}</li>
 * <li>{@link #setXPathExpressionResultCardinality(XPathExpressionResultCardinality)} to indicate that the value to be
 * retrieved is mandatory blocking, mandatory or optional. See {@link XPathExpressionResultCardinality} for more detail.
 * </li>
 * <li>A String identifier that gives an identification to this expression. This can be a description or word that
 * describes what this expression selects.</li>
 * </ul>
 * 
 * @see WebServiceMessageXPathExtractor
 * 
 * @author Koen Serneels
 */
public class WebServiceMessageXPathExpressionMetaData {

	/**
	 * Indicates to what the XPath expression evaluates. This is either the value of an element or a complete node (in
	 * that case an XML fragment is selected)
	 */
	public static enum XPathExpressionType {
		VALUE, NODE;
	}

	/**
	 * Indicates where the given XPath expression starts. Is it from the top of the SOAP document then
	 * {@link #SOAP_ENVELOPE} should be used. Is it relative to the SOAP Body, then {@link #SOAP_BODY} should be used.
	 */
	public static enum XPathExpressionScope {
		SOAP_ENVELOPE, SOAP_BODY;
	}

	/**
	 * The value selected by an XPath expression can be absent. When optional it does not matter if the value is absent.
	 * This is for optional fields that we know about that they can be absent. Use {@link #OPTIONAL} in that case. If
	 * result values are expected to be always there, but the fact that if they are not is not severe (we would like to
	 * know about it, but don't block processing for example) use {@link #MANDATORY}. If a mandatory value is absent and
	 * we like to block processing because of it, use {@link #MANDATORY_BLOCKING}.
	 * <p>
	 * 
	 * <b>default is {@link #MANDATORY}</b>
	 */
	public static enum XPathExpressionResultCardinality {
		MANDATORY_BLOCKING, MANDATORY, OPTIONAL;
	}

	private final String xPathKey;
	private final String unparsedXPathExpression;
	private XPathExpression xPathExpression;
	private XPathExpressionType xPathExpressionType = XPathExpressionType.VALUE;
	private XPathExpressionScope xPathExpressionScope = XPathExpressionScope.SOAP_BODY;
	private XPathExpressionResultCardinality xPathExpressionResultCardinality = XPathExpressionResultCardinality.MANDATORY;

	public WebServiceMessageXPathExpressionMetaData(String xPathExpression, String xPathKey) {
		if (StringUtils.isBlank(xPathKey)) {
			throw new IllegalArgumentException("xPathKey cannot be absent");
		}
		this.xPathKey = xPathKey;
		this.unparsedXPathExpression = xPathExpression;
		XPathExpressionFactoryBean xPathExpressionFactoryBean = new XPathExpressionFactoryBean();
		xPathExpressionFactoryBean.setExpression(xPathExpression);
		xPathExpressionFactoryBean.afterPropertiesSet();

		try {
			this.xPathExpression = xPathExpressionFactoryBean.getObject();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public XPathExpressionScope getXPathExpressionScope() {
		return xPathExpressionScope;
	}

	public XPathExpression getXPathExpression() {
		return xPathExpression;
	}

	public XPathExpressionType getXPathExpressionType() {
		return xPathExpressionType;
	}

	public String getXPathKey() {
		return xPathKey;
	}

	public void setXPathExpressionType(XPathExpressionType xPathExpressionType) {
		this.xPathExpressionType = xPathExpressionType;
	}

	public void setXPathExpressionScope(XPathExpressionScope xPathExpressionScope) {
		this.xPathExpressionScope = xPathExpressionScope;
	}

	public void setXPathExpressionResultCardinality(XPathExpressionResultCardinality xPathExpressionResultCardinality) {
		this.xPathExpressionResultCardinality = xPathExpressionResultCardinality;
	}

	public XPathExpressionResultCardinality getXPathExpressionResultCardinality() {
		return xPathExpressionResultCardinality;
	}

	public String getUnparsedXpathExpression() {
		return unparsedXPathExpression;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (that == null || getClass() != that.getClass()) {
			return false;
		}

		return new EqualsBuilder().append(this.getXPathKey(),
				((WebServiceMessageXPathExpressionMetaData) that).getXPathKey()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getXPathKey()).toHashCode();
	}
}
