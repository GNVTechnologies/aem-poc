package com.adobe.aem.guides.poc.core.services.impl;

import com.adobe.aem.guides.poc.core.services.FormsEndpointService;
import com.adobe.aem.guides.poc.core.services.impl.FormsEndpointServiceImpl.FormsEndpointConfiguration;
import com.mongodb.util.JSON;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

@Component(service = FormsEndpointService.class)
@Designate(ocd = FormsEndpointConfiguration.class)
@ServiceDescription("POC Forms Endpoint Service")
public class FormsEndpointServiceImpl implements FormsEndpointService {

  private static final Logger logger = LoggerFactory.getLogger(FormsEndpointServiceImpl.class);

  private String endPointServerUrl = "";
  private String apiKey = "";
  private static final String CONTENT_TYPE = "application/json";
  private static final String APPLICATION_ID = "MYAPPLICATIONID";


  @Reference
  private transient HttpClientBuilderFactory clientBuilderFactory;

  private transient CloseableHttpClient httpClient;

  @Activate
  @Modified
  protected void activate(final FormsEndpointConfiguration config) {
    endPointServerUrl = config.endpoint_server_url();
    apiKey = config.api_key();
    //get other items / info from config framework as required

    final HttpClientBuilder builder = clientBuilderFactory.newBuilder();
    final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000)
        .setSocketTimeout(30000).build();
    builder.setDefaultRequestConfig(requestConfig);
    httpClient = builder.build();
  }

  @Override
  public void postDocument(String path) {

    //Lets start transaction
    // uuid
    String uuid = UUID.randomUUID().toString();
    logger.info("Processing Document Payload Path:"+path);
    logger.info("Processing Document UUID:"+uuid);
    //
    try {
      JSONObject transactionPayload = getTrasactionPayload(uuid);
      //Add headers
      // perhaps separate method to return all the headers to be used with setHeaders method call
      String postResponse = Request.Post(endPointServerUrl)
          .addHeader("Content-Type", CONTENT_TYPE)
          .addHeader("WF_senderApplicationId", APPLICATION_ID)
          .addHeader("WF-ESIGN-apiKey", apiKey)
          .setHeaders()
          .bodyString(transactionPayload.toString(), ContentType.create(CONTENT_TYPE)).execute().returnContent().asString();
      //process response
      if(StringUtils.isEmpty(postResponse)){
        throw new Exception("Error starting transaction ");
      }
      //
      JSONObject transactionResponseObj = new JSONObject(postResponse);
      logger.info(transactionResponseObj.toString());
      //get GUID from transaction response

      //Go onto add documents and post again

      //do other things
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private JSONObject getTrasactionPayload(String msgId) throws JSONException {
    JSONObject payload = new JSONObject();
    payload.put("external-id", msgId);
    //add other things to transaction msg
    return payload;
  }

  private void addDocuments(String path) {
    //do the business logic
  }

  @ObjectClassDefinition(name = "POC Forms Endpoint Service Configuration")
  public @interface FormsEndpointConfiguration {

    @AttributeDefinition(
        name = "End Point Server Url",
        description = "End Point Server Url",
        defaultValue = "http://localhost",
        type = AttributeType.STRING)
    String endpoint_server_url();

    @AttributeDefinition(
        name = "API Key",
        description = "API Key",
        defaultValue = "xyz-1234",
        type = AttributeType.STRING)
    String api_key();
    //Add other configuration items here
  }
}
