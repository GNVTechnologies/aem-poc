package com.adobe.aem.guides.poc.core.workflow.process;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample workflow process that sets an <code>approve</code> property to the payload based on the process argument value.
 */
@Component(service = WorkflowProcess.class, property = {"process.label=POC Handle Exception (JavaProcess)" })
public class ExceptionHandledProcess implements WorkflowProcess {

  private static final String TYPE_JCR_PATH = "JCR_PATH";
  public static final String EXP_DATA_1 = "exp-data-1";
  public static final String EXP_ERROR = "exp-error";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    logger.error("Exception Handled NOT throwing exception");
    MetaDataMap wfd = item.getWorkflow().getWorkflowData().getMetaDataMap();
    try {
      wfd.put(EXP_DATA_1, "ExceptionHandledProcess Sample Data 1");
      if (true) throw new WorkflowException("programatically thrown exception");
    } catch (Exception e) {
      wfd.put(EXP_ERROR, e.getMessage());
    }
  }

  private boolean readArgument(MetaDataMap args) {
    String argument = args.get("PROCESS_ARGS", "false");
    return argument.equalsIgnoreCase("true");
  }
}