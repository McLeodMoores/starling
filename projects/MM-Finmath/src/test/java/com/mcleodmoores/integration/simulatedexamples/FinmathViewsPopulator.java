/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.DISCOUNT_CURVE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

@Scriptable
public class FinmathViewsPopulator extends AbstractTool<ToolContext> {
  /** Name of the default calculation configurations. */
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FinmathViewsPopulator.class);
  /** The default maximum delta calculation period */
  private static final long MAX_DELTA_PERIOD = 30000L;
  /** The default maximum full calculation period */
  private static final long MAX_FULL_PERIOD = 30000L;
  /** The default minimum delta calculation period */
  private static final long MIN_DELTA_PERIOD = 500L;
  /** The default minimum full calculation period */
  private static final long MIN_FULL_PERIOD = 500L;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new FinmathViewsPopulator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getTestViewDefinition("Test"));
  }

  private ViewDefinition getTestViewDefinition(final String portfolioName) {
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);

    final ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalc.addSpecificRequirement(new ValueRequirement(DISCOUNT_CURVE, ComputationTargetSpecification.NULL, ValueProperties.builder().with(CURVE, "Test").get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  /**
   * Adds a list of value requirement names to a calculation configuration for a particular security type.
   * @param calcConfiguration The calculation configuration
   * @param securityType The security type
   * @param valueRequirementNames The value requirement names to add
   */
  private static void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType,
      final String[] valueRequirementNames) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
    }
  }

  /**
   * Gets the id for a portfolio name.
   * @param portfolioName The portfolio name
   * @return The unique id of the portfolio
   */
  private UniqueId getPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      LOGGER.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  /**
   * Stores a view definition in the config master.
   * @param viewDefinition The view definition
   */
  private void storeViewDefinition(final ViewDefinition viewDefinition) {
    final ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

}
