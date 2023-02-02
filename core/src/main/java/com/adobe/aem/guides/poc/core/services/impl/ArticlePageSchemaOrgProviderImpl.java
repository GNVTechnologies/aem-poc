package com.adobe.aem.guides.poc.core.services.impl;

import com.adobe.aem.guides.poc.core.services.SchemaOrgProvider;
import org.apache.sling.api.resource.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(property = {
"service.ranking:Integer=101",
"schema.type=article"
}, service = SchemaOrgProvider.class)
@ServiceDescription("Article Page Schema Org Service Implementation")
public class ArticlePageSchemaOrgProviderImpl implements SchemaOrgProvider {

  private static final Logger logger = LoggerFactory.getLogger(ArticlePageSchemaOrgProviderImpl.class);

  public String getType() {
    return "article";
  }

  @Override
  public JSONObject getSchema(Resource resource) {
    //Sample json
    /*
        {
      "@context": "https://schema.org",
      "@type": "Article",
      "headline": "Article headline",
      "image": [
        "https://example.com/photos/1x1/photo.jpg",
        "https://example.com/photos/4x3/photo.jpg",
        "https://example.com/photos/16x9/photo.jpg"
       ],
      "datePublished": "2015-02-05T08:00:00+08:00",
           "author": [{
          "@type": "Organization",
          "name": "Spectrum",
          "url": "https://www.spectrum.com/"
        }]
    }
     */
    JSONObject schemaOrg = new JSONObject();
    try {
      schemaOrg.put("@context", "https://schema.org");
      schemaOrg.put("@type", "Article");
      schemaOrg.put("headline", "Test Head Line");
      schemaOrg.put("datePublished", "2015-02-05T08:00:00+08:00");
      JSONArray authors = new JSONArray();
      JSONObject author = new JSONObject();
      author.put("@type", "Organization");
      author.put("name", "Spectrum");
      author.put("url","https://www.spectrum.com/");
      authors.put(author);
      schemaOrg.put("author", authors);
    } catch (Exception e) {
      //ignore
    }
    return schemaOrg;
  }
}
