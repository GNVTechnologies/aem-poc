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

import com.adobe.aem.guides.poc.core.workflow.process.ExceptionHandledProcess;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
//https://experienceleague.adobe.com/docs/experience-manager-65/developing/extending-aem/extending-workflows/workflows-customizing-extending.html?lang=en#developing-process-step-implementations

@Component(service = { Servlet.class })
@SlingServletPaths({
    "/bin/poc/processdoc"
})
@ServiceDescription("Document Processor Workflow Servlet")
public class DocumentProcessorWorkflowServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String modelId = "/var/workflow/models/poc--workflow-1";
    private static final String payload = "/content/dam/we-retail/en/features/tracking.png";

    public static final String STATUS = "status";

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {

        logger.info("Servlet Get Method");

        String someInputData = "someinputdata";
        ResourceResolver resourceResolver = req.getResourceResolver();
        JsonObject returnDoc = processDocument(resourceResolver, someInputData);
        resp.setContentType("text/plain");
        resp.getWriter().write("Data:");
        resp.getWriter().write(returnDoc.toString());
        resp.getWriter().flush();
        logger.info("Servlet Complete");

    }

    private JsonObject processDocument(ResourceResolver resourceResolver, String inputData) {
        WorkflowSession wfSession = resourceResolver.adaptTo(WorkflowSession.class);
        JsonObject processResult = new JsonObject();
        try {
            WorkflowModel model = wfSession.getModel(modelId);
            WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", payload);
            Workflow wf = wfSession.startWorkflow(model, wfData);
            logger.info("Workflow ID: " + wf.getId());
            int loop = 0;
            int count = 120;
            String wfId = wf.getId();
            processResult.addProperty("wfid", wfId);
            String status = "OTHER";
            while ((loop < count)) {
                ((Session)wfSession.adaptTo(Session.class)).refresh(false);
                wf = wfSession.getWorkflow(wfId);
                status = wf.getState();
                logger.info("WF Status:" + wf.getId() + ":" + status);
                logger.info("Is Active:" + wf.getId() + ":" + wf.isActive());
                if(StringUtils.equals("RUNNING", status)) {
                    try {
                        Thread.sleep(1000);
                        loop++;
                        logger.info("Loop Counter:" + loop);
                    } catch (Exception e) {
                        processResult.addProperty("error", e.getMessage());
                    }
                } else {
                    break;
                }
            }
            //workflow either completed successfully or possible errored and still in "RUNNING" state

            //Properly written workflow processes should handle all exceptions and set error messages into workflow data map
            MetaDataMap wfd = wf.getWorkflowData().getMetaDataMap();
            processResult.addProperty("wfstatus", status);
            processResult.addProperty(ExceptionHandledProcess.EXP_DATA_1, wfd.getOrDefault(ExceptionHandledProcess.EXP_DATA_1, "defaultvalue").toString());
            if(wfd.containsKey(ExceptionHandledProcess.EXP_ERROR)) {
                processResult.addProperty("exception", wfd.get(ExceptionHandledProcess.EXP_ERROR).toString());
            }

            //Misbehaved Workflow Steps may throw uncaught exception, error out but still keep the workflow in RUNNING state.
            // these may be terminated programatically, but for now, lets just get information about them and add to response
            JsonArray failures = new JsonArray();
            wf.getWorkItems().forEach((item) -> {
                logger.info((item.getItemSubType()));
                JsonObject failedItem = new JsonObject();
                failedItem.addProperty("id", item.getId());
                MetaDataMap data = item.getMetaDataMap();
                data.keySet().forEach((key) -> {
                    failedItem.addProperty(key, data.get(key).toString());
                });
                failures.add(failedItem);
            });
            processResult.add("failures", failures);
        } catch (WorkflowException e) {
            e.printStackTrace();
            processResult.addProperty("error", e.getMessage());
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            processResult.addProperty("error", e.getMessage());
        } catch (RepositoryException e) {
            e.printStackTrace();
            processResult.addProperty("error", e.getMessage());
        }
        logger.info(processResult.toString());
        return processResult;
    }

}
