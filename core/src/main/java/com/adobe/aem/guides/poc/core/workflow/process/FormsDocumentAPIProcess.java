package com.adobe.aem.guides.poc.core.workflow.process;

import com.adobe.aem.guides.poc.core.services.FormsEndpointService;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample workflow process that calls the Forms API Submit Service to send stuff over to a JSON End point.
 */
@Component(service = WorkflowProcess.class, property = {"process.label=POC Forms Document API Process (JavaProcess)" })
public class FormsDocumentAPIProcess implements WorkflowProcess {

  private static final String TYPE_JCR_PATH = "JCR_PATH";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Reference
  private transient FormsEndpointService formsEndpointService;

  public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    logger.info("FormsDocumentAPIProcess Start");
    formsEndpointService.postDocument(item.getWorkflowData().getPayload().toString());
    logger.info("FormsDocumentAPIProcess End");

  }

  private boolean readArgument(MetaDataMap args) {
    String argument = args.get("PROCESS_ARGS", "false");
    return argument.equalsIgnoreCase("true");
  }
}