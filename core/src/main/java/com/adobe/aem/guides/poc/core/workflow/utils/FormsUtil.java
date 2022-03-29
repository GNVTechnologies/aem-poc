package com.adobe.aem.guides.poc.core.workflow.utils;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.fd.workflow.utils.PropertyResolver;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormsUtil {

  private static Logger logger = LoggerFactory.getLogger(FormsUtil.class);
  private static final String REPO_PREFIX = "crx://";

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  public static String getAbsolutePath(WorkItem workItem, String key)
      throws WorkflowException
  {
    String absPath = (String)PropertyResolver.getInstance().getPropertyValue(workItem, key, String.class);
    String retPath = null;
    if ((absPath != null) && (absPath.startsWith("/"))) {
      retPath = "crx://" + absPath;
    }

    logger.debug("getAbsolutePath: " + key + " = " + retPath);
    return retPath;
  }

}
