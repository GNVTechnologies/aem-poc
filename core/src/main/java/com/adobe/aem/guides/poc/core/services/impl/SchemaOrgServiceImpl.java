package com.adobe.aem.guides.poc.core.services.impl;

import com.adobe.aem.guides.poc.core.services.SchemaOrgProvider;
import com.adobe.aem.guides.poc.core.services.SchemaOrgService;
import com.adobe.aem.guides.poc.core.services.impl.SchemaOrgServiceImpl.ServiceOrgConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.Resource;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = SchemaOrgService.class)
@Designate(ocd = ServiceOrgConfiguration.class)
@ServiceDescription("Schema Org Service Service Implementation")
public class SchemaOrgServiceImpl implements SchemaOrgService {

  private static final Logger logger = LoggerFactory.getLogger(SchemaOrgServiceImpl.class);

  private Map<String, SchemaOrgProvider> providerMap = new HashMap<>();


  @Activate
  @Modified
  protected void activate(final ServiceOrgConfiguration config) {

    //get other items / info from config framework as required
  }

  @Override
  public JSONObject getSchema(Resource resource) {
    SchemaOrgProvider provider = providerMap.get("article");
    if(provider != null) {
      return provider.getSchema(resource);
    }
    return null;
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  protected void addSchemaOrgProvider(SchemaOrgProvider provider) {
    logger.info("Adding A Provider:"+provider.getType());
    providerMap.put(provider.getType(), provider);
  }

  protected void removeSchemaOrgProvider(SchemaOrgProvider provider) {
    logger.info("Removing A Provider:"+provider.getType());
    providerMap.remove(provider.getType());
  }

  @ObjectClassDefinition(name = "Schema Org Service Service Configuration")
  public @interface ServiceOrgConfiguration {

    //Add other configuration items here
    @AttributeDefinition(
        name = "Template/Provider Mapping Configuration",
        description = "Template/Provider Mapping Configuration",
        type = AttributeType.STRING)
    String[] providerMapping() default {};

  }
}
