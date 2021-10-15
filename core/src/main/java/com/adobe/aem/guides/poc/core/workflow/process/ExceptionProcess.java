package com.adobe.aem.guides.poc.core.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

import org.osgi.service.component.annotations.*;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Sample workflow process that sets an <code>approve</code> property to the payload based on the process argument value.
 */
@Component(service = WorkflowProcess.class, property = {"process.label=POC Throw Exception (JavaProcess)" })
public class ExceptionProcess implements WorkflowProcess {

  private static final String TYPE_JCR_PATH = "JCR_PATH";

  public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    if (true) throw new WorkflowException("programatically thrown exception");
    WorkflowData workflowData = item.getWorkflowData();
    if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
      String path = workflowData.getPayload().toString() + "/jcr:content";
      try {
        Session jcrSession = session.adaptTo(Session.class);
        Node node = (Node) jcrSession.getItem(path);
        if (node != null) {
          node.setProperty("approved", readArgument(args));
          jcrSession.save();
        }
      } catch (RepositoryException e) {
        throw new WorkflowException(e.getMessage(), e);
      }
    }
  }

  private boolean readArgument(MetaDataMap args) {
    String argument = args.get("PROCESS_ARGS", "false");
    return argument.equalsIgnoreCase("true");
  }
}