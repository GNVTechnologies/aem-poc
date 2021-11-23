package com.adobe.aem.guides.poc.core.forms;

import com.adobe.forms.common.service.ContentType;
import com.adobe.forms.common.service.DataOptions;
import com.adobe.forms.common.service.DataProvider;
import com.adobe.forms.common.service.DataXMLOptions;
import com.adobe.forms.common.service.DataXMLProvider;
import com.adobe.forms.common.service.FormsException;
import com.adobe.forms.common.service.PrefillData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class PrefillAdaptiveForm implements DataProvider {

  private static final Logger logger = LoggerFactory.getLogger(PrefillAdaptiveForm.class);

  public String getServiceName() {
    return "POCCustomAemFormsPrefillService";
  }

  public String getServiceDescription() {
    return "POC Custom Aem  Forms Prefill Service";
  }

  public PrefillData getPrefillData(final DataOptions dataOptions) throws FormsException {
    PrefillData prefillData = new PrefillData() {
      public InputStream getInputStream() {
        return getData(dataOptions);
      }
      public ContentType getContentType() {
        return ContentType.XML;
      }
    };
    return prefillData;
  }

  private InputStream getData(DataOptions dataOptions) throws FormsException {
    try {
      Resource aemFormContainer = dataOptions.getFormResource();
      ResourceResolver resolver = aemFormContainer.getResourceResolver();
      Map<String, Object> requestParams = dataOptions.getExtras();
      //migrate request params over to the prefill params
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      Element rootElement = doc.createElement("data");
      doc.appendChild(rootElement);
      Element firstNameElement = doc.createElement("fname");
      firstNameElement.setTextContent(loggedinUser.getProperty("profile/givenName")[0].getString());
      InputStream inputStream = new ByteArrayInputStream(rootElement.getTextContent().getBytes());
      return inputStream;
    } catch (Exception e) {
      logger.error("Error while creating prefill data", e);
      throw new FormsException(e);
    }
  }
}