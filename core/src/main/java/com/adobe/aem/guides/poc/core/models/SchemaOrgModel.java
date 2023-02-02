package com.adobe.aem.guides.poc.core.models;

import com.adobe.aem.guides.poc.core.services.SchemaOrgService;
import javax.annotation.PostConstruct;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Model(adaptables = Resource.class)
public class SchemaOrgModel {
    private static final Logger logger = LoggerFactory.getLogger(SchemaOrgModel.class);

    @OSGiService
    private SchemaOrgService schemaOrgService;

    @SlingObject
    private Resource currentResource;

    /* Sling Model Usage
    <sly
     data-sly-use.test="com.adobe.aem.guides.poc.core.models.SchemaOrgModel">
        <script type="application/ld+json">
        ${test.schema @ context='unsafe'}
        </script>
    </sly>
     */
    private String message;

    @PostConstruct
    protected void init() {
        message = "default";
        if(schemaOrgService != null) {
            try {
                message = schemaOrgService.getSchema(currentResource).toString(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSchema() {
        return message;
    }



}
