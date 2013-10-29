package be.error.wsproxy.interceptors.internalchain;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionResultCardinality;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionScope;
import be.error.wsproxy.interceptors.logging.WebServiceMessageXPathExpressionMetaData.XPathExpressionType;

@Test
public class WebServiceMessageXPathExpressionMetaDataTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWebServiceMessageXPathExpressionMetaDataNoXpathExpression() {
		new WebServiceMessageXPathExpressionMetaData(null, "test");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWebServiceMessageXPathExpressionMetaDataNoKey() {
		new WebServiceMessageXPathExpressionMetaData("test", null);
	}

	public void testWebServiceMessageXPathExpressionMetaData() {
		Assert.assertEquals(new WebServiceMessageXPathExpressionMetaData("//*", "testKey"),
				new WebServiceMessageXPathExpressionMetaData("/*", "testKey"));

		WebServiceMessageXPathExpressionMetaData webServiceMessageXPathExpressionMetaData = new WebServiceMessageXPathExpressionMetaData(
				"/*", "testKey");

		Assert.assertNotNull(webServiceMessageXPathExpressionMetaData.getXPathExpression());
		Assert.assertEquals(webServiceMessageXPathExpressionMetaData.getXPathExpressionResultCardinality(),
				XPathExpressionResultCardinality.MANDATORY);
		Assert.assertEquals(webServiceMessageXPathExpressionMetaData.getXPathExpressionType(),
				XPathExpressionType.VALUE);
		Assert.assertEquals(webServiceMessageXPathExpressionMetaData.getXPathExpressionScope(),
				XPathExpressionScope.SOAP_BODY);
	}
}
