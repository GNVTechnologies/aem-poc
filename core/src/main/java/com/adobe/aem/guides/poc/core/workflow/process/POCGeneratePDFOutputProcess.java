package com.adobe.aem.guides.poc.core.workflow.process;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.fd.output.api.AcrobatVersion;
import com.adobe.fd.output.api.OutputService;
import com.adobe.fd.output.api.PDFOutputOptions;
import com.adobe.aem.guides.poc.core.workflow.utils.FormsUtil;
import com.adobe.fd.workflow.utils.DocumentUtils;
import com.adobe.fd.workflow.utils.PropertyResolver;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = WorkflowProcess.class, property = {"process.label=POC GeneratePDFOutputProcess (JavaProcess)" })

public class POCGeneratePDFOutputProcess implements WorkflowProcess
{
  private static Logger logger = LoggerFactory.getLogger(POCGeneratePDFOutputProcess.class);
  private static final String TEMPLATE_PARAM = "urlOrFileName";
  private static final String DATA_PARAM = "data";
  private static final String CONTENT_ROOT_PARAM = "contentroot";
  private static final String LINEARIZED_PARAM = "linearized";
  private static final String LOCALE_PARAM = "locale";
  private static final String ACROBAT_VERSION_PARAM = "acrobatversion";
  private static final String TAGGED_PARAM = "tagged";
  private static final String XCI_PARAM = "xci";
  private static final String OUTPUT_PARAM = "outputpdf";

  @Reference
  OutputService outputService;

  @Override
  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
      throws WorkflowException {
    logger.debug("Executing Generate Non Interactive PDF step");
    Document dataDoc = null;
    try
    {
      String template = FormsUtil.getAbsolutePath(workItem, "urlOrFileName");
      dataDoc = (Document)PropertyResolver.getInstance().getPropertyValue(workItem, "data", Document.class);
      PDFOutputOptions options = createPDFOutputOptions(workItem, workflowSession, metaDataMap);
      logger.debug("Invoking output service to generate non interactive PDF.");
      Document document = this.outputService.generatePDFOutput(template, dataDoc, options);
      logger.debug("Successfully generated non interactive PDF.");

      PropertyResolver.getInstance().setPropertyValue(workItem, workflowSession, "outputpdf", document);
      logger.debug("Successfully saved generated PDF.");
    }
    catch (Exception e)
    {
      logger.debug("Error while generation of non interactive PDF", e);
      throw new WorkflowException("Error while generation of non interactive PDF");
    }
    finally
    {
      DocumentUtils.closeDocumentIfRequired(dataDoc);
    }
    logger.debug("Successfully finished Generate Non Interactive PDF step");
  }

  private PDFOutputOptions createPDFOutputOptions(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
      throws WorkflowException
  {
    PDFOutputOptions options = new PDFOutputOptions();

    options.setContentRoot(FormsUtil.getAbsolutePath(workItem, "contentroot"));
    if (metaDataMap.get("tagged", Boolean.class) != null)
    {
      logger.debug("tagged=" + metaDataMap.get("tagged", Boolean.class));
      options.setTaggedPDF(((Boolean)metaDataMap.get("tagged", Boolean.class)).booleanValue());
    }
    if (metaDataMap.get("linearized", Boolean.class) != null)
    {
      logger.debug("linearized=" + metaDataMap.get("linearized", Boolean.class));
      options.setLinearizedPDF(((Boolean)metaDataMap.get("linearized", Boolean.class)).booleanValue());
    }
    String localeValue = (String)PropertyResolver.getInstance().getPropertyValue(workItem, "locale", String.class);
    if (localeValue != null)
    {
      logger.debug("locale=" + localeValue);
      options.setLocale(localeValue);
    }
    String acrobatVersion = (String)metaDataMap.get("acrobatversion", String.class);
    logger.debug("acrobatversion=" + acrobatVersion);
    if (acrobatVersion != null) {
      options.setAcrobatVersion(AcrobatVersion.valueOf(acrobatVersion));
    }
    //Set retainSignatureFields
    if (metaDataMap.get("retainsignfields", Boolean.class) != null)
    {
      logger.debug("retainsignfields=" + metaDataMap.get("retainsignfields", Boolean.class));
      options.setRetainUnsignedSignatureFields(((Boolean)metaDataMap.get("retainsignfields", Boolean.class)).booleanValue());
    }
    options.setXci((Document)PropertyResolver.getInstance().getPropertyValue(workItem, "xci", Document.class));
    return options;
  }

}
