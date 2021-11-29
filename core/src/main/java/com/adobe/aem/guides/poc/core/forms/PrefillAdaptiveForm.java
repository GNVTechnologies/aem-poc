package com.adobe.aem.guides.poc.core.forms;

import com.adobe.forms.common.service.ContentType;
import com.adobe.forms.common.service.DataOptions;
import com.adobe.forms.common.service.DataProvider;
import com.adobe.forms.common.service.FormsException;
import com.adobe.forms.common.service.PrefillData;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
  private final String PREFILL_PREFIX = "prefill-";


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
    Resource aemFormContainer = dataOptions.getFormResource();
    ResourceResolver resolver = aemFormContainer.getResourceResolver();
    Map<String, Object> params = dataOptions.getExtras();

    boolean prefillDataAvailable = false;
    StringBuffer prefillData = new StringBuffer();
    prefillData.append("<afData>");
    prefillData.append("<afUnboundData>");
    prefillData.append("<data>");
    if(params!=null && !params.isEmpty()){
      for(String paramName: params.keySet()){
        String paramValue = params.get(paramName).toString();
        if(paramName.startsWith(PREFILL_PREFIX)) {
          prefillDataAvailable = true;
          prefillData.append("<")
              .append(paramName.split(PREFILL_PREFIX)[1])
              .append(">")
              .append(paramValue)
              .append("</")
              .append(paramName.split(PREFILL_PREFIX)[1])
              .append(">");
        }

      }
    }
    prefillData.append("</data>");
    prefillData.append("</afUnboundData>");
    prefillData.append("</afData>");
    logger.info("Form Prefill Data:"+prefillData.toString());
    InputStream inputStream = new ByteArrayInputStream(prefillData.toString().getBytes());
    return inputStream;
  }


  private InputStream getData2(DataOptions dataOptions) throws FormsException {
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
      //firstNameElement.setTextContent(loggedinUser.getProperty("profile/givenName")[0].getString());
      firstNameElement.setTextContent("test first name");
      rootElement.appendChild(firstNameElement);
      InputStream inputStream = new ByteArrayInputStream(rootElement.getTextContent().getBytes());
      return inputStream;
    } catch (Exception e) {
      logger.error("Error while creating prefill data", e);
      throw new FormsException(e);
    }
  }
}