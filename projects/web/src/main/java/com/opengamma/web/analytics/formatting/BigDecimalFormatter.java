/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.DoubleValueDecimalPlaceFormatter;
import com.opengamma.web.server.conversion.DoubleValueFormatter;
import com.opengamma.web.server.conversion.DoubleValueSignificantFiguresFormatter;
import com.opengamma.web.server.conversion.DoubleValueSizeBasedDecimalPlaceFormatter;
import com.opengamma.web.server.conversion.PercentageValueSignificantFiguresFormatter;

/**
 *
 */
/* package */ class BigDecimalFormatter extends AbstractFormatter<BigDecimal> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigDecimalFormatter.class);
  private static final Map<String, DoubleValueFormatter> FORMATTERS = Maps.newHashMap();
  private static final DoubleValueFormatter DEFAULT_FORMATTER = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;
  private static final DoubleValueFormatter DEFAULT_CCY_FORMATTER = DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT;

  static {
    // General
    FORMATTERS.put(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.YIELD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.INSTANTANEOUS_FORWARD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.COST_OF_CARRY, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Pricing
    FORMATTERS.put(ValueRequirementNames.PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.GAMMA_PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.DV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.GAMMA_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.RR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.IR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.JUMP_TO_DEFAULT, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.PAR_RATE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.VALUE_THETA, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.SECURITY_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    FORMATTERS.put(ValueRequirementNames.SECURITY_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    FORMATTERS.put(ValueRequirementNames.UNDERLYING_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    FORMATTERS.put(ValueRequirementNames.UNDERLYING_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    FORMATTERS.put(ValueRequirementNames.DAILY_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));

    // PnL
    FORMATTERS.put(ValueRequirementNames.PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.DAILY_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.MTM_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Greeks
    FORMATTERS.put(ValueRequirementNames.DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.DELTA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.STRIKE_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.GAMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.STRIKE_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.GAMMA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.GAMMA_P_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VEGA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VARIANCE_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VEGA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CARRY_RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.BUCKETED_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.BUCKETED_GAMMA_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.BUCKETED_IR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.YIELD_CURVE_JACOBIAN, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VARIANCE_ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.SPEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.SPEED_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VARIANCE_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.DVANNA_DVOL, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VOMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.VARIANCE_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FORWARD_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FORWARD_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.DUAL_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.DUAL_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FORWARD_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FORWARD_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.FORWARD_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.DRIFTLESS_THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Position/value greeks
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Series analysis
    FORMATTERS.put(ValueRequirementNames.SKEW, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // VaR
    FORMATTERS.put(ValueRequirementNames.HISTORICAL_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.HISTORICAL_VAR_STDDEV, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    FORMATTERS.put(ValueRequirementNames.CONDITIONAL_HISTORICAL_VAR,
        DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Capital Asset Pricing
    FORMATTERS.put(ValueRequirementNames.CAPM_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Traditional Risk-Reward
    FORMATTERS.put(ValueRequirementNames.SHARPE_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.TREYNOR_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.JENSENS_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.TOTAL_RISK_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.WEIGHT, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // Bonds
    FORMATTERS.put(ValueRequirementNames.CLEAN_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.YTM, PercentageValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.MARKET_YTM, PercentageValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.MARKET_DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.MACAULAY_DURATION, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.CONVEXITY, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.Z_SPREAD, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.CONVERTION_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.IMPLIED_REPO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.GROSS_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.NET_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    FORMATTERS.put(ValueRequirementNames.NS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    FORMATTERS.put(ValueRequirementNames.NSS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Options
    FORMATTERS.put(ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // FX
    FORMATTERS.put(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Fixed income
    FORMATTERS.put(ValueRequirementNames.FIXED_RATE, DoubleValueDecimalPlaceFormatter.NON_CCY_3DP);

    // FX
    FORMATTERS.put(ValueRequirementNames.PAY_DISCOUNT_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.RECEIVE_DISCOUNT_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.PAY_DISCOUNT_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    FORMATTERS.put(ValueRequirementNames.RECEIVE_DISCOUNT_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
  }

  private final ResultsFormatter.CurrencyDisplay _currencyDisplay;

  /* package */ BigDecimalFormatter(final ResultsFormatter.CurrencyDisplay currencyDisplay) {
    super(BigDecimal.class);
    ArgumentChecker.notNull(currencyDisplay, "currencyDisplay");
    _currencyDisplay = currencyDisplay;
    addFormatter(new Formatter<BigDecimal>(Format.HISTORY) {
      @Override
      protected Object formatValue(final BigDecimal value, final ValueSpecification valueSpec, final Object inlineKey) {
        return getDoubleValueFormatter(valueSpec).getRoundedValue(value);
      }
    });
    addFormatter(new Formatter<BigDecimal>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final BigDecimal value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatCell(value, valueSpec, inlineKey);
      }
    });
  }

  private static void addBulkConversion(final String valueRequirementFieldNamePattern, final DoubleValueFormatter conversionSettings) {
    final Pattern pattern = Pattern.compile(valueRequirementFieldNamePattern);
    for (final Field field : ValueRequirementNames.class.getFields()) {
      if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC) &&
          field.isSynthetic() == false &&
          String.class.equals(field.getType()) && pattern.matcher(field.getName()).matches()) {
        String fieldValue;
        try {
          fieldValue = (String) field.get(null);
          FORMATTERS.put(fieldValue, conversionSettings);
        } catch (final Exception e) {
          LOGGER.debug("Unexpected exception initializing formatter", e);
        }
      }
    }
  }

  private static DoubleValueFormatter getDoubleValueFormatter(final ValueSpecification valueSpec) {
    if (valueSpec == null) {
      return DEFAULT_FORMATTER;
    }
    final DoubleValueFormatter valueNameFormatter = FORMATTERS.get(valueSpec.getValueName());
    if (valueNameFormatter != null) {
      return valueNameFormatter;
    }
    if (valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY) != null) {
      return DEFAULT_CCY_FORMATTER;
    }
    return DEFAULT_FORMATTER;
  }

  @Override
  public DataType getDataType() {
    return DataType.DOUBLE;
  }

  @Override
  public String formatCell(final BigDecimal value, final ValueSpecification valueSpec, final Object inlineKey) {
    final DoubleValueFormatter formatter = getDoubleValueFormatter(valueSpec);
    final String formattedNumber = formatter.format(value);
    return formatter.isCurrencyAmount() && _currencyDisplay == DISPLAY_CURRENCY ?
        formatWithCurrency(formattedNumber, valueSpec) :
          formattedNumber;
  }

  private static String formatWithCurrency(final String formattedNumber, final ValueSpecification valueSpec) {
    final Set<String> currencyValues = valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY);
    return currencyValues == null || currencyValues.isEmpty() ?
        formattedNumber :
          currencyValues.iterator().next() + " " + formattedNumber;
  }
}
