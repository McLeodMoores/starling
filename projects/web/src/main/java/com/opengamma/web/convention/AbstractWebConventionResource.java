/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

import java.util.Map.Entry;

import org.joda.beans.impl.flexi.FlexiBean;

import com.mcleodmoores.web.json.convention.BondConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.CmsLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.CompoundingIborLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.DepositConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.FixedLegRollDateConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.FxForwardAndSwapConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.FxSpotConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.IborIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.InflationLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.OisLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.OnArithmeticAverageLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.OnCompoundedLegRollDateConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.OvernightIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.PriceIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.RollDateFraConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.SwapFixedLegConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.SwapIndexConventionJsonBuilder;
import com.mcleodmoores.web.json.convention.VanillaIborLegConventionJsonBuilder;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful convention resources.
 */
public abstract class AbstractWebConventionResource extends AbstractPerRequestWebResource<WebConventionData> {

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
   * @param conventionMaster
   *          the convention master, not null
   */
  protected AbstractWebConventionResource(final ConventionMaster conventionMaster) {
    super(new WebConventionData());
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    data().setConventionMaster(conventionMaster);
    initializeMetaData();
    initializeJsonBuilders();
  }

  // init meta-data
  private void initializeMetaData() {
    for (final Entry<String, Class<? extends ManageableConvention>> entry : _conventionTypesProvider.getTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
      data().getClassNameMap().put(entry.getValue().getSimpleName(), entry.getValue());
    }
  }

  private void initializeJsonBuilders() {
    data().getJsonBuilderMap().put(BondConvention.class, BondConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(CMSLegConvention.class, new CmsLegConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(CompoundingIborLegConvention.class, new CompoundingIborLegConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(DepositConvention.class, DepositConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(FixedLegRollDateConvention.class, FixedLegRollDateConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(FXSpotConvention.class, FxSpotConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(FXForwardAndSwapConvention.class, new FxForwardAndSwapConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(IborIndexConvention.class, IborIndexConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(InflationLegConvention.class, new InflationLegConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(OISLegConvention.class, new OisLegConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(ONArithmeticAverageLegConvention.class, new OnArithmeticAverageLegConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(ONCompoundedLegRollDateConvention.class, new OnCompoundedLegRollDateConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(OvernightIndexConvention.class, OvernightIndexConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(PriceIndexConvention.class, PriceIndexConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(RollDateFRAConvention.class, new RollDateFraConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(SwapIndexConvention.class, new SwapIndexConventionJsonBuilder(data().getConventionMaster()));
    data().getJsonBuilderMap().put(SwapFixedLegConvention.class, SwapFixedLegConventionJsonBuilder.INSTANCE);
    data().getJsonBuilderMap().put(VanillaIborLegConvention.class, new VanillaIborLegConventionJsonBuilder(data().getConventionMaster()));
  }

  /**
   * Creates the resource.
   *
   * @param parent
   *          the parent resource, not null
   */
  protected AbstractWebConventionResource(final AbstractWebConventionResource parent) {
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
    out.put("uris", new WebConventionUris(data()));
    return out;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the convention types provider.
   *
   * @return the convention types provider
   */
  public ConventionTypesProvider getConventionTypesProvider() {
    return _conventionTypesProvider;
  }

}
