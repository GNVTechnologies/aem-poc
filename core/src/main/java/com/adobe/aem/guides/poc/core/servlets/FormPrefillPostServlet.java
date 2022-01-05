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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingAllMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */

@Component(service = { Servlet.class })
@SlingServletPaths({
    "/bin/poc/form/prefill"
})
@ServiceDescription("POC Form Prefill Post Servlet")
public class FormPrefillPostServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String FORM_ID = "formId";
    private static final String REDIRCT_URL = "/content";
    private final String PREFILL_PREFIX = "prefill2-";
    private static final String FORM_URL = "/content/forms/af/test-2-form.html?wcmmode=disabled";

    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        String formId = req.getRequestParameter(FORM_ID).getString();
        if(formId == null) {
            resp.sendRedirect(REDIRCT_URL);
            return;
        }

        boolean prefillDataAvailable = false;
        StringBuffer prefillData = new StringBuffer();
        prefillData.append("<afData>");
        prefillData.append("<afUnboundData>");
        prefillData.append("<data>");
        RequestParameterMap params = req.getRequestParameterMap();
        if(params!=null && !params.isEmpty()){
            Set<Entry<String, RequestParameter[]>> parameterSet = params.entrySet();
            Iterator<Entry<String, RequestParameter[]>> iter = parameterSet.iterator();
            for(Entry<String,RequestParameter[]> entry : parameterSet){
                String paramName = entry.getKey();
                String paramValue = entry.getValue()[0].getString();
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
        if(prefillDataAvailable) {
            //req.setAttribute("data", prefillData.toString());
            req.getSession().setAttribute("data", prefillData.toString());
            logger.info("Set Form Prefill Data:"+prefillData.toString());
            resp.sendRedirect(FORM_URL);
            return;
        }
        //catch all
        resp.sendRedirect(REDIRCT_URL);
    }

}
