package com.adobe.aem.guides.poc.core.services;

import org.apache.sling.api.resource.Resource;
import org.json.JSONObject;

public interface SchemaOrgService {
  public JSONObject getSchema(Resource resource);
}
