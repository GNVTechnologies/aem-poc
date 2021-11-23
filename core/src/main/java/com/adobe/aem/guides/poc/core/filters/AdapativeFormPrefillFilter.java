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
package com.adobe.aem.guides.poc.core.filters;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.sling.api.request.*;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.component.propertytypes.ServiceVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Simple servlet filter component that logs incoming requests.
 */
@Component(service = Filter.class,
           property = {
                   EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
           })
@ServiceDescription("Adaptive Form Prefill Filter")
@ServiceRanking(-699)
@ServiceVendor("Adobe")
public class AdapativeFormPrefillFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response,
        final FilterChain filterChain) throws IOException, ServletException {

        //RequestParameter prefillData = slingRequest.getRequestParameter("prefillData");
        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;

        if ("GET".equals(slingRequest.getMethod())
            && ((slingRequest.getPathInfo().startsWith("/content/dam/formsanddocuments"))
            ||(slingRequest.getPathInfo().startsWith("/content/forms")))
            //    && (prefillData != null)
            && (slingRequest.getAttribute("data") == null)) {
            boolean prefillDataAvailable = false;
            StringBuffer prefillData = new StringBuffer();
            prefillData.append("<afData>");
            prefillData.append("<afUnboundData>");
            prefillData.append("<data>");
            RequestParameterMap params = slingRequest.getRequestParameterMap();
            if(params!=null && !params.isEmpty()){
                Set<Entry<String, RequestParameter[]>> parameterSet = params.entrySet();
                Iterator<Entry<String, RequestParameter[]>> iter = parameterSet.iterator();
                for(Entry<String,RequestParameter[]> entry : parameterSet){
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue()[0].getString();
                    if(paramName.startsWith("prefill-")) {
                        prefillDataAvailable = true;
                        prefillData.append("<")
                            .append(paramName.split("prefill-")[1])
                            .append(">")
                            .append(paramValue)
                            .append("</")
                            .append(paramName.split("prefill-")[1])
                            .append(">");
                    }

                }
            }
            prefillData.append("</data>");
            prefillData.append("</afUnboundData>");
            prefillData.append("</afData>");
            logger.info("Form Prefill Data:"+prefillData.toString());
            if(prefillDataAvailable) {
                slingRequest.setAttribute("data", prefillData.toString());
                logger.info("Set Form Prefill Data:"+prefillData.toString());
            }
        }
        filterChain.doFilter(request, response);
    }

    public void doFilter2(ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        //RequestParameter prefillData = slingRequest.getRequestParameter("prefillData");
        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;

        if ("POST".equals(slingRequest.getMethod())
            && (slingRequest.getPathInfo().startsWith("/content/dam/formsanddocuments"))
        //    && (prefillData != null)
            && (slingRequest.getAttribute("data") == null)) {
            String prefillData="<afData>" +
                "<afUnboundData>" +
                "<data>" +
                "<first_name>"+ "Tyler" + "</first_name>" +
                "<last_name>"+ "Durden " + "</last_name>" +
                "<gender>"+ "Male" + "</gender>" +
                "<location>"+ "Texas" + "</location>" +
                "</data>" +
                "</afUnboundData>" +
                "</afData>";
            logger.info("Form Prefill Data:"+prefillData);
            slingRequest.setAttribute("data", prefillData);
            logger.info("Set Form Prefill Data:"+prefillData);
            request = new AdaptiveFormsPostRequestWrapper((HttpServletRequest)request);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("Adaptive Form Prefill Filter...Init");
    }

    @Override
    public void destroy() {
    }

}