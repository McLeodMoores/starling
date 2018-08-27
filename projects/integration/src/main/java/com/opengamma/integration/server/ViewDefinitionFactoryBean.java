/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates a ViewDefinition.
 */
public class ViewDefinitionFactoryBean extends SingletonFactoryBean<ViewDefinition> {

  private String _name;
  private String _portfolioScheme;
  private String _portfolioId;
  private String _userName;
  private Map<String, Map<String, String[]>> _portfolioRequirements;
  private Long _minDeltaCalculationPeriod;
  private Long _maxDeltaCalculationPeriod;
  private Long _minFullCalculationPeriod;
  private Long _maxFullCalculationPeriod;
  private Map<String, Set<String[]>> _specificRequirements;
  private Map<String, Map<String, String[]>> _tradeRequirements;
  private Map<String, Map<String, String>> _defaultProperties;
  private String _defaultCurrency = "GBP";

  public void setName(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setPortfolioScheme(final String portfolioScheme) {
    _portfolioScheme = portfolioScheme;
  }

  public String getPortfolioScheme() {
    return _portfolioScheme;
  }

  public void setPortfolioId(final String portfolioId) {
    _portfolioId = portfolioId;
  }

  public String getPortfolioId() {
    return _portfolioId;
  }

  public void setUserName(final String userName) {
    _userName = userName;
  }

  public String getUserName() {
    return _userName;
  }

  public void setUser(final UserPrincipal user) {
    setUserName(user.getUserName());
  }

  public Long getMinDeltaCalculationPeriod() {
    return _minDeltaCalculationPeriod;
  }

  public void setMinDeltaCalculationPeriod(final Long period) {
    _minDeltaCalculationPeriod = period;
  }

  public Long getMaxDeltaCalculationPeriod() {
    return _maxDeltaCalculationPeriod;
  }

  public void setMaxDeltaCalculationPeriod(final Long period) {
    _maxDeltaCalculationPeriod = period;
  }

  public void setDeltaCalculationPeriod(final Long deltaCalcPeriod) {
    setMinDeltaCalculationPeriod(deltaCalcPeriod);
    setMaxDeltaCalculationPeriod(deltaCalcPeriod);
  }

  public Long getMinFullCalculationPeriod() {
    return _minFullCalculationPeriod;
  }

  public void setMinFullCalculationPeriod(final Long period) {
    _minFullCalculationPeriod = period;
  }

  public Long getMaxFullCalculationPeriod() {
    return _maxFullCalculationPeriod;
  }

  public void setMaxFullCalculationPeriod(final Long period) {
    _maxFullCalculationPeriod = period;
  }

  public void setFullCalculationPeriod(final Long fullCalcPeriod) {
    setMinFullCalculationPeriod(fullCalcPeriod);
    setMaxFullCalculationPeriod(fullCalcPeriod);
  }

  public String getDefaultCurrency() {
    return _defaultCurrency;
  }

  public void setDefaultCurrency(final String defaultCurrency) {
    _defaultCurrency = defaultCurrency;
  }

  /**
   * Sets the value definitions as calcConfig -> (securityType -> requirementName).
   *
   * @param portfolioRequirements value definitions to set
   */
  public void setPortfolioRequirements(final Map<String, Map<String, String[]>> portfolioRequirements) {
    _portfolioRequirements = portfolioRequirements;
  }

  public Map<String, Map<String, String[]>> getPortfolioRequirements() {
    return _portfolioRequirements;
  }

  public void setTradeRequirements(final Map<String, Map<String, String[]>> tradeRequirements) {
    _tradeRequirements = tradeRequirements;
  }

  public Map<String, Map<String, String[]>> getTradeRequirements() {
    return _tradeRequirements;
  }

  public void setDefaultProperties(final Map<String, Map<String, String>> defaultProperties) {
    _defaultProperties = defaultProperties;
  }

  public Map<String, Map<String, String>> getDefaultProperties() {
    return _defaultProperties;
  }

  public void setSpecificRequirements(final Map<String, Set<String[]>> specificRequirements) {
    _specificRequirements = specificRequirements;
  }

  public Map<String, Set<String[]>> getSpecificRequirements() {
    return _specificRequirements;
  }

  /**
   * Parses a requirement written in the form {@code valueName[constraint=value;constraint=value;...]}.
   *
   * @param requirement string to parse
   * @return the value name and constraints
   */
  private Pair<String, ValueProperties> parseValueRequirement(final String requirement) {
    // NOTE -- This is not at all robust as it is just a quick measure to drop constraints into the Spring config XML
    final int i = requirement.indexOf('[');
    if (i < 0) {
      return Pairs.of(requirement, ValueProperties.none());
    }
    final String valueName = requirement.substring(0, i);
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (final String constraint : requirement.substring(i + 1, requirement.length() - 1).split(";")) {
      final String[] pair = constraint.split("=");
      builder.with(pair[0], pair[1]);
    }
    return Pairs.of(valueName, builder.get());
  }

  @Override
  protected ViewDefinition createObject() {
    ViewDefinition viewDefinition;
    if (getPortfolioScheme() != null && getPortfolioId() != null) {
      viewDefinition = new ViewDefinition(getName(), UniqueId.of(getPortfolioScheme(), getPortfolioId()), getUserName());
      if (getPortfolioRequirements() != null) {
        for (final Map.Entry<String, Map<String, String[]>> config : getPortfolioRequirements().entrySet()) {
          final ViewCalculationConfiguration calcConfig = getOrCreateCalcConfig(viewDefinition, config.getKey());
          for (final Map.Entry<String, String[]> security : config.getValue().entrySet()) {
            for (final String value : security.getValue()) {
              final Pair<String, ValueProperties> requirement = parseValueRequirement(value);
              calcConfig.addPortfolioRequirement(security.getKey(), requirement.getFirst(), requirement.getSecond());
            }
          }
        }
      }
    } else {
      viewDefinition = new ViewDefinition(getName(), getUserName());
    }
    viewDefinition.setMinDeltaCalculationPeriod(getMinDeltaCalculationPeriod());
    viewDefinition.setMaxDeltaCalculationPeriod(getMaxDeltaCalculationPeriod());
    viewDefinition.setMinFullCalculationPeriod(getMinFullCalculationPeriod());
    viewDefinition.setMaxFullCalculationPeriod(getMaxFullCalculationPeriod());
    if (getSpecificRequirements() != null) {
      for (final Entry<String, Set<String[]>> config : getSpecificRequirements().entrySet()) {
        final ViewCalculationConfiguration calcConfig = getOrCreateCalcConfig(viewDefinition, config.getKey());
        for (final String[] entry : config.getValue()) {
          if (entry.length < 4) {
            throw new OpenGammaRuntimeException("Not enough members of array in specific requirements. Need [0]=Name [1]=ComputationTargetType [2]=UniqueIdScheme [3]=UniqueIdValue");
          }
          final Pair<String, ValueProperties> requirement = parseValueRequirement(entry[0]);
          final String type = entry[1];
          final String scheme = entry[2];
          final String value = entry[3];
          calcConfig.addSpecificRequirement(new ValueRequirement(requirement.getFirst(), ComputationTargetType.parse(type), UniqueId.of(scheme, value), requirement.getSecond()));
        }
      }
    }
    if (getDefaultCurrency() != null) {
      viewDefinition.setDefaultCurrency(Currency.of(getDefaultCurrency()));
    }

    if (getDefaultProperties() != null) {
      for (final Entry<String, Map<String, String>> defaultProperties : getDefaultProperties().entrySet()) {
        final ViewCalculationConfiguration calcConfig = getOrCreateCalcConfig(viewDefinition, defaultProperties.getKey());
        final ValueProperties.Builder properties = ValueProperties.builder();
        for (final Entry<String, String> property : defaultProperties.getValue().entrySet()) {
          properties.with(property.getKey(), property.getValue());
        }
        calcConfig.setDefaultProperties(properties.get());
      }
    }
    for (final ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      if (calcConfig.getDefaultProperties().getValues(ValuePropertyNames.CURRENCY) == null) {
        calcConfig.setDefaultProperties(calcConfig.getDefaultProperties().copy().with(ValuePropertyNames.CURRENCY, getDefaultCurrency()).get());
      }
    }
    return viewDefinition;
  }

  private ViewCalculationConfiguration getOrCreateCalcConfig(final ViewDefinition viewDefinition, final String configName) {
    ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(configName);
    if (calcConfig == null) {
      calcConfig = new ViewCalculationConfiguration(viewDefinition, configName);
      viewDefinition.addViewCalculationConfiguration(calcConfig);
    }
    return calcConfig;
  }

}
