/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Selects volatility cubes for manipulation.
 */
public class VolatilityCubeSelector extends Selector {

  /* package */ VolatilityCubeSelector(final Set<String> calcConfigNames,
                                       final Set<String> names,
                                       final Set<Currency> currencies,
                                       final Pattern nameMatchPattern,
                                       final Pattern nameLikePattern) {
    super(calcConfigNames,
          names,
          currencies,
          nameMatchPattern,
          nameLikePattern);
  }

  @Override
  boolean matches(final ValueSpecification valueSpecification) {
    if (!ValueRequirementNames.VOLATILITY_CUBE.equals(valueSpecification.getValueName())) {
      return false;
    }
    final Currency currency = Currency.parse(valueSpecification.getTargetSpecification().getUniqueId().getValue());
    final String cube = valueSpecification.getProperties().getStrictValue(ValuePropertyNames.CUBE);
    if (cube == null) {
      return false;
    }
    return matches(cube, currency);
  }

  /**
   * Mutable builder for {@link VolatilityCubeSelector} instances.
   */
  public static class Builder extends Selector.Builder {

    /* package */ Builder(final Scenario scenario) {
      super(scenario);
    }

    public VolatilityCubeManipulatorBuilder apply() {
      return new VolatilityCubeManipulatorBuilder(selector(), getScenario());
    }

    @Override
    public Builder named(final String... names) {
      super.named(names);
      return this;
    }

    @Override
    public Builder currencies(final String... codes) {
      super.currencies(codes);
      return this;
    }

    @Override
    public Builder nameMatches(final String regex) {
      super.nameMatches(regex);
      return this;
    }

    /**
     * This is package scoped for testing
     * @return A selector built from this builder's data
     */
    /* package */ VolatilityCubeSelector selector() {
      return new VolatilityCubeSelector(getScenario().getCalcConfigNames(),
                                        getNames(),
                                        getCurrencies(),
                                        getNameMatchPattern(),
                                        getNameLikePattern());
    }
  }
}
