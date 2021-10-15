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

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.commons.jcr.JcrConstants;
import java.io.IOException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.apache.sling.servlets.annotations.SlingServletPaths;

import javax.jcr.Session;
import javax.jcr.Node;

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
@Component(service = { Servlet.class })
@SlingServletPaths({
    "/bin/poc/kickoffworkflow"
})
@ServiceDescription("Simple Workflow Servlet")
public class SimpleWorkflowServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String modelId = "/var/workflow/models/poc--workflow-1";
    private static final String payload = "/content/dam/we-retail/en/features/tracking.png";

    public static final String STATUS = "status";

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {

        String reqId = null;

        if(req.getRequestParameter("reqId") != null) {
            reqId = req.getRequestParameter("reqId").getString();
        }

        WorkflowSession wfSession = req.getResource().getResourceResolver().adaptTo(WorkflowSession.class);
        try {
            WorkflowModel model = wfSession.getModel(modelId);
            WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", payload);
            Workflow wf = wfSession.startWorkflow(model, wfData);
            logger.info("Servlet: " + wf.getId());
            int loop = 0;
            int count = 120;
            boolean isDone = false;
            String wfId = wf.getId();
            while (loop < count) {
                try {
                    ((Session)wfSession.adaptTo(Session.class)).refresh(false);
                    wf = wfSession.getWorkflow(wfId);
                    //wfSession.resumeWorkflow(wf);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String status = wfSession.getWorkflow(wfId).getState(); //wf.getState();
                Node wfInstanceNode = (Node) ((Session) wfSession.adaptTo(Session.class)).getItem(wfId);
                //Resource resource = req.getResourceResolver().getResource(wfId);
                //Node wfInstanceNode = resource.adaptTo(Node.class);
                if (wfInstanceNode != null && wfInstanceNode.hasProperty(STATUS)) {
                    status = wfInstanceNode.getProperty(STATUS).getString();
                    //status = JcrUtils.getStringProperty((Session)wfSession.adaptTo(Session.class), wfId+"/status", "NA");
                    logger.info("Got WF status from node directly1:" + status);
                    logger.info("Got WF Version from node directly1:" + wfInstanceNode
                        .getProperty("modelVersion").getString());

                }
                logger.info(status);
                logger.info(
                    "Is Active:" + wf.getId() + ":" + wfSession.getWorkflow(wfId).isActive());
                if (status.equals("COMPLETED"))
                    break;
                else {
                    try {
                        Thread.sleep(1000);
                        loop++;
                        logger.info("Loop Counter:" + loop);
                    } catch (Exception e) {
                    }
                }
            }
            resp.setContentType("text/plain");
            //resp.getWriter()
            //    .write("Title = " + resource.getValueMap().get(JcrConstants.JCR_TITLE));
            logger.info("Simple Workflow Servlet");

        } catch (WorkflowException e) {
            e.printStackTrace();
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }
}
