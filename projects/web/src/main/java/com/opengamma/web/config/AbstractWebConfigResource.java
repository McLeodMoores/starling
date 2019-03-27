/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.util.Map.Entry;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.json.ViewDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilityCubeDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilitySurfaceDefinitionJSONBuilder;
import com.opengamma.web.json.VolatilitySurfaceSpecificationJSONBuilder;

/**
 * Abstract base class for RESTful config resources.
 */
public abstract class AbstractWebConfigResource extends AbstractPerRequestWebResource<WebConfigData> {

  /**
   * Config xml form parameter name
   */
  protected static final String CONFIG_XML = "configXML";
  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "configs/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "configs/json/";
  /**
   * The Config Types provider
   */
  private final ConfigTypesProvider _configTypesProvider = ConfigTypesProvider.getInstance();

  /**
   * Creates the resource.
   *
   * @param configMaster
   *          the config master, not null
   */
  protected AbstractWebConfigResource(final ConfigMaster configMaster) {
    super(new WebConfigData());
    ArgumentChecker.notNull(configMaster, "configMaster");
    data().setConfigMaster(configMaster);
    initializeMetaData();
    initializeJSONBuilders();
  }

  // init meta-data
  private void initializeMetaData() {
    for (final Entry<String, Class<?>> entry : _configTypesProvider.getConfigTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
    }
  }

  private void initializeJSONBuilders() {
    data().getJsonBuilderMap().put(ViewDefinition.class, ViewDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilityCubeDefinition.class, VolatilityCubeDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceDefinition.class, VolatilitySurfaceDefinitionJSONBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VolatilitySurfaceSpecification.class, VolatilitySurfaceSpecificationJSONBuilder.INSTANCE);
  }

  /**
   * Creates the resource.
   *
   * @param parent
   *          the parent resource, not null
   */
  protected AbstractWebConfigResource(final AbstractWebConfigResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    out.put("uris", new WebConfigUris(data()));
    return out;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the configTypesProvider.
   *
   * @return the configTypesProvider
   */
  public ConfigTypesProvider getConfigTypesProvider() {
    return _configTypesProvider;
  }

}
