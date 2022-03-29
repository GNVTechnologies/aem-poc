/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aem.guides.poc.core.servlets;

import com.adobe.aem.guides.poc.core.servlets.JsonFromAssetServlet.FormsMetadataConfiguration;
import com.day.cq.dam.api.Asset;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = { Servlet.class })
@Designate(ocd = FormsMetadataConfiguration.class)
@SlingServletPaths({
    "/bin/poc/jsonfromasset"
})
@ServiceDescription("Forms Configuration Servlet")
public class JsonFromAssetServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String formsConfig = "/content/dam/forms-config.json";
    private static final String DEFAULT_FORMS_CONFIG = "/content/dam/forms-config.json";


    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {

        logger.info("Servlet Get Method");
        ResourceResolver resourceResolver = req.getResourceResolver();
        Resource configResource = resourceResolver.getResource(formsConfig);
        resp.setContentType("application/json");
        JsonObject errorResponse = new JsonObject();
        if(configResource != null) {
            Asset configAsset = configResource.adaptTo(Asset.class);
            logger.info("Got config asset %s", configAsset.getPath());
            try {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(configAsset.getOriginal().getStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    resp.getWriter().write(inputLine);
                }
                in.close();
                resp.getWriter().flush();
                return;
            } catch (Exception e) {
                //catch all
                logger.error(e.getMessage(), e);
                errorResponse.addProperty("error", e.getMessage());
            }
        } else {
            errorResponse.addProperty("error", "Json Cannot Be Found:"+formsConfig);
        }
        resp.getWriter().write(errorResponse.toString());
        resp.getWriter().flush();
        logger.info("Servlet Complete");
    }

    @Activate
    @Modified
    protected void activate(final FormsMetadataConfiguration config) {
        formsConfig = config.forms_metadata_dam_path();
        if(formsConfig == null) {
            formsConfig = DEFAULT_FORMS_CONFIG;
        }
        logger.info("Forms Metadata Initialization....Config:" + formsConfig);
        //get other items / info from config framework as required
    }


    @ObjectClassDefinition(name = "Forms Metadata Configuration")
    public @interface FormsMetadataConfiguration {

        @AttributeDefinition(
            name = "Forms Metadata DAM Path",
            description = "Forms Metadata DAM Path",
            defaultValue = "/content/dam/forms-config.json",
            type = AttributeType.STRING)
        String forms_metadata_dam_path();
        //Add other configuration items here
    }

}
