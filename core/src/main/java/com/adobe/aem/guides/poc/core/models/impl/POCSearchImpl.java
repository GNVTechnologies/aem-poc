package com.adobe.aem.guides.poc.core.models.impl;

import java.util.Objects;
import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Style;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;


import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search model implementation.
 */
@Model(adaptables = SlingHttpServletRequest.class,
    adapters = {Search.class, ComponentExporter.class},
    resourceType = POCSearchImpl.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class POCSearchImpl implements Search {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The resource type.
   */
  protected static final String RESOURCE_TYPE = "poc/components/search";

  protected static final String CONTEXT_CONFIG = "context-config";

  protected static final String SEARCH_ROOT_PATH = "search-root-path";


  @Self
  private SlingHttpServletRequest request;

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource currentResource;

  /**
   * The current page.
   */
  @ScriptVariable
  private Page currentPage;

  /**
   * The current style.
   */
  @ScriptVariable
  private Style currentStyle;
  /**
   * Default number of results to show.
   */
  public static final int PROP_RESULTS_SIZE_DEFAULT = 10;

  /**
   * Default minimum search term length.
   */
  public static final int PROP_SEARCH_TERM_MINIMUM_LENGTH_DEFAULT = 3;

  /**
   * The relative path between this component and the containing page.
   */
  private String relativePath;

  /**
   * The number of results to return.
   */
  private int resultsSize;

  /**
   * The minimum search term length.
   */
  private int searchTermMinimumLength;

  /**
   * The path of the search root page.
   */
  private String searchRootPagePath;

  /**
   * Initialize the model.
   */
  @PostConstruct
  private void initModel() {
    logger.info("Search Root Model Init. Current Page:"+currentPage.getPath());
    logger.info("Search Root Model Init. Current Resource"+currentResource.getPath());
    Resource resource = currentPage.getContentResource();
    if (Objects.nonNull(resource) && Objects.nonNull(resource.adaptTo(
        ConfigurationBuilder.class))
        && Objects.nonNull(resource.adaptTo(ConfigurationBuilder.class).name(CONTEXT_CONFIG))) {
      ValueMap valueMap = resource.adaptTo(ConfigurationBuilder.class)
          .name(CONTEXT_CONFIG)
          .asValueMap();
      if (Objects.isNull(valueMap) || !(valueMap.containsKey(SEARCH_ROOT_PATH))) {
        return;
      }
      searchRootPagePath = valueMap.get(SEARCH_ROOT_PATH, String.class);
    }
    resultsSize = currentStyle.get(PN_RESULTS_SIZE, PROP_RESULTS_SIZE_DEFAULT);
    searchTermMinimumLength = currentStyle.get(PN_SEARCH_TERM_MINIMUM_LENGTH, PROP_SEARCH_TERM_MINIMUM_LENGTH_DEFAULT);
    Resource currentResource = request.getResource();
    this.relativePath = Optional.ofNullable(currentPage.getPageManager().getContainingPage(currentResource))
        .map(Page::getPath)
        .map(path -> StringUtils.substringAfter(currentResource.getPath(), path))
        .orElse(null);
    logger.info("Search Root Model Init Complete:" + searchRootPagePath);

  }

  @Override
  public int getResultsSize() {
    return resultsSize;
  }

  @Override
  public int getSearchTermMinimumLength() {
    return searchTermMinimumLength;
  }

  @NotNull
  @Override
  public String getRelativePath() {
    return relativePath;
  }

  public String getSearchRootPagePath() {
    logger.info("Search Root Page Path:" + searchRootPagePath);
    return searchRootPagePath;
  }

  @NotNull
  @Override
  public String getExportedType() {
    return request.getResource().getResourceType();
  }

}
