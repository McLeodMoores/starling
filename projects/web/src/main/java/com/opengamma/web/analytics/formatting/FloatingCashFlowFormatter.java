/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.ACCRUAL_YEAR_FRACTION;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.DISCOUNTED_PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.DISCOUNTED_PROJECTED_PAYMENT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.END_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.END_FIXING_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.FIXED_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.FIXING_FRACTIONS;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.FORWARD_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.GEARING;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.INDEX_TERM;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.NOTIONAL;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.PAYMENT_DATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.PAYMENT_DISCOUNT_FACTOR;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.PAYMENT_TIME;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.PROJECTED_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.SPREAD;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.START_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows.START_FIXING_DATES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FloatingLegCashFlows;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FloatingCashFlowFormatter extends AbstractFormatter<FloatingLegCashFlows> {
  private static final String[] COLUMN_LABELS = { NOTIONAL, SPREAD, GEARING, INDEX_TERM,
      PAYMENT_DATE, PAYMENT_TIME, START_ACCRUAL_DATES, END_ACCRUAL_DATES, ACCRUAL_YEAR_FRACTION, START_FIXING_DATES, END_FIXING_DATES, FIXING_FRACTIONS,
      FIXED_RATE, PAYMENT_DISCOUNT_FACTOR, PAYMENT_AMOUNT, DISCOUNTED_PAYMENT_AMOUNT,
      FORWARD_RATE, PROJECTED_AMOUNT, DISCOUNTED_PROJECTED_PAYMENT };
  private static final String X_LABELS = "xLabels";
  private static final String Y_LABELS = "yLabels";
  private static final String MATRIX = "matrix";

  private final DoubleFormatter _doubleFormatter;
  private final RateFormatter _rateFormatter;
  private final CurrencyAmountFormatter _currencyAmountFormatter;

  /**
   * @param doubleFormatter
   *          formats the discount factors
   * @param rateFormatter
   *          formats the zero rates
   * @param currencyAmountFormatter
   *          formats the currency amounts
   */
  /* package */ FloatingCashFlowFormatter(final DoubleFormatter doubleFormatter, final RateFormatter rateFormatter,
      final CurrencyAmountFormatter currencyAmountFormatter) {
    super(FloatingLegCashFlows.class);
    _doubleFormatter = doubleFormatter;
    _currencyAmountFormatter = currencyAmountFormatter;
    _rateFormatter = rateFormatter;
    addFormatter(new Formatter<FloatingLegCashFlows>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final FloatingLegCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final FloatingLegCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "FloatingCashFlows";
  }

  /**
   * Transforms the details object to an amount that can be displayed.
   * 
   * @param value
   *          the FX forward details
   * @param valueSpec
   *          the value specification
   * @return the data
   */
  /* package */ Map<String, Object> formatExpanded(final FloatingLegCashFlows value, final ValueSpecification valueSpec) {
    final int columnCount = COLUMN_LABELS.length;
    final String[] yLabels = new String[value.getNumberOfCashFlows()];
    Arrays.fill(yLabels, "");
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[value.getNumberOfCashFlows()][columnCount];
    for (int i = 0; i < value.getNumberOfCashFlows(); i++) {
      values[i][0] = _currencyAmountFormatter.formatCell(value.getNotionals().get(i), valueSpec, null);
      values[i][1] = _doubleFormatter.formatCell(value.getSpreads().get(i), valueSpec, null);
      values[i][2] = _doubleFormatter.formatCell(value.getGearings().get(i), valueSpec, null);
      values[i][3] = value.getIndexTenors().get(i).toFormattedString();
      values[i][4] = value.getPaymentDates().get(i).toString();
      values[i][5] = _doubleFormatter.formatCell(value.getPaymentTimes().get(i), valueSpec, null);
      values[i][6] = value.getAccrualStart().get(i).toString();
      values[i][7] = value.getAccrualEnd().get(i).toString();
      values[i][8] = _doubleFormatter.formatCell(value.getAccrualYearFractions().get(i), valueSpec, null);
      values[i][9] = value.getFixingStart().get(i).toString();
      values[i][10] = value.getFixingEnd().get(i).toString();
      values[i][11] = _doubleFormatter.formatCell(value.getFixingYearFractions().get(i), valueSpec, null);
      final Double fixedRate = value.getFixedRates().get(i);
      final Double df = value.getPaymentDiscountFactors().get(i);
      final CurrencyAmount amount = value.getPaymentAmounts().get(i);
      final CurrencyAmount discountedAmount = value.getDiscountedPaymentAmounts().get(i);
      values[i][12] = fixedRate == null ? "-" : _rateFormatter.formatCell(fixedRate, valueSpec, null);
      values[i][13] = df == null ? "-" : _doubleFormatter.formatCell(df, valueSpec, null);
      values[i][14] = amount == null ? "-" : _currencyAmountFormatter.formatCell(amount, valueSpec, null);
      values[i][15] = discountedAmount == null ? "-" : _currencyAmountFormatter.formatCell(discountedAmount, valueSpec, null);
      final Double forwardRate = value.getForwardRates().get(i);
      final CurrencyAmount projectedAmount = value.getProjectedAmounts().get(i);
      final CurrencyAmount discountedProjectedAmount = value.getDiscountedProjectedAmounts().get(i);
      values[i][16] = forwardRate == null ? "-" : _rateFormatter.formatCell(forwardRate, valueSpec, null);
      values[i][17] = projectedAmount == null ? "-" : _currencyAmountFormatter.formatCell(projectedAmount, valueSpec, null);
      values[i][18] = discountedProjectedAmount == null ? "-" : _currencyAmountFormatter.formatCell(discountedProjectedAmount, valueSpec, null);
    }
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
