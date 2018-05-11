/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

import java.util.Map.Entry;

import org.joda.beans.impl.flexi.FlexiBean;

import com.mcleodmoores.web.json.BondConventionJsonBuilder;
import com.mcleodmoores.web.json.IborIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.OvernightIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.PriceIndexConventionJsonBuilder;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful convention resources.
 */
public abstract class AbstractWebConventionResource
extends AbstractPerRequestWebResource<WebConventionData> {

  /**
   * Config XML form parameter name.
   */
  protected static final String CONFIG_XML = "configXML";
  /**
   * Config JSON form parameter name.
   */
  protected static final String CONFIG_JSON = "configJSON";
  /**
   * HTML ftl directory.
   */
  protected static final String HTML_DIR = "conventions/html/";
  /**
   * JSON ftl directory.
   */
  protected static final String JSON_DIR = "conventions/json/";

  private final ConventionTypesProvider _conventionTypesProvider = ConventionTypesProvider.getInstance();

  /**
   * Creates the resource.
   *
   * @param conventionMaster  the convention master, not null
   */
  protected AbstractWebConventionResource(final ConventionMaster conventionMaster) {
    super(new WebConventionData());
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    data().setConventionMaster(conventionMaster);
    initializeMetaData();
    initializeJsonBuilders();
  }

  //init meta-data
  private void initializeMetaData() {
    for (final Entry<String, Class<? extends ManageableConvention>> entry : _conventionTypesProvider.getTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
      data().getClassNameMap().put(entry.getValue().getSimpleName(), entry.getValue());
    }
  }

  private void initializeJsonBuilders() {
    data().getJsonBuilderMap().put(BondConvention.class, BondConventionJsonBuilder.INSTANCE);
//    data().getJsonBuilderMap().put(FXSpotConvention.class, FxSpotConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(IborIndexConvention.class, IborIndexConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(OvernightIndexConvention.class, OvernightIndexConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(PriceIndexConvention.class, PriceIndexConventionJsonBuilder.INSTANCE);
  }

  /**
   * Creates the resource.
   *
   * @param parent  the parent resource, not null
   */
  protected AbstractWebConventionResource(final AbstractWebConventionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    out.put("uris", new WebConventionUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention types provider.
   *
   * @return the convention types provider
   */
  public ConventionTypesProvider getConventionTypesProvider() {
    return _conventionTypesProvider;
  }

}
