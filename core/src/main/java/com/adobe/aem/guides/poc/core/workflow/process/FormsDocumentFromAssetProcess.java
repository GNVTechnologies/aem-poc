package com.adobe.aem.guides.poc.core.workflow.process;

import com.adobe.aem.guides.poc.core.services.FormsEndpointService;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample workflow process that calls the Forms API Submit Service to send stuff over to a JSON End point.
 */
@Component(service = WorkflowProcess.class, property = {"process.label=POC Forms Document From Asset Process (JavaProcess)" })
public class FormsDocumentFromAssetProcess implements WorkflowProcess {

  private static final String TYPE_JCR_PATH = "JCR_PATH";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    logger.info("Start");
    String xdpPath = "/content/dam/formsanddocuments/templates-folder/Classic.xdp";
    ResourceResolver resolver = session.adaptTo(ResourceResolver.class);
    Resource resource  = resolver.getResource(xdpPath);
    //AssetManager assetManager = resolver.adaptTo(AssetManager.class);
    Asset asset = resource.adaptTo((Asset.class)); //assetManager.getAssetForBinary(xdpPath);
    logger.info("Asset From Process:"+asset);
    logger.info("FormsDocumentAPIProcess End");
  }

  private boolean readArgument(MetaDataMap args) {
    String argument = args.get("PROCESS_ARGS", "false");
    return argument.equalsIgnoreCase("true");
  }
}