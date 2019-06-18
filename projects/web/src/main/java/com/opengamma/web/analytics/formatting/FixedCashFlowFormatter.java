/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.DISCOUNTED_PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.DISCOUNT_FACTOR;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.END_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.FIXED_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.NOTIONAL;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.PAYMENT_TIME;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.PAYMENT_YEAR_FRACTION;
import static com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows.START_ACCRUAL_DATES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FixedLegCashFlows;

/**
 *
 */
public class FixedCashFlowFormatter extends AbstractFormatter<FixedLegCashFlows> {
  private static final String[] COLUMN_LABELS = { NOTIONAL, FIXED_RATE, START_ACCRUAL_DATES, END_ACCRUAL_DATES, PAYMENT_YEAR_FRACTION,
      PAYMENT_AMOUNT, PAYMENT_TIME, DISCOUNT_FACTOR, DISCOUNTED_PAYMENT_AMOUNT };
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
  /* package */ FixedCashFlowFormatter(final DoubleFormatter doubleFormatter, final RateFormatter rateFormatter,
      final CurrencyAmountFormatter currencyAmountFormatter) {
    super(FixedLegCashFlows.class);
    _doubleFormatter = doubleFormatter;
    _currencyAmountFormatter = currencyAmountFormatter;
    _rateFormatter = rateFormatter;
    addFormatter(new Formatter<FixedLegCashFlows>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final FixedLegCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final FixedLegCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "FixedCashFlows";
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
  /* package */ Map<String, Object> formatExpanded(final FixedLegCashFlows value, final ValueSpecification valueSpec) {
    final int columnCount = COLUMN_LABELS.length;
    final String[] yLabels = new String[value.getNumberOfCashFlows()];
    Arrays.fill(yLabels, "");
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[value.getNumberOfCashFlows()][columnCount];
    for (int i = 0; i < value.getNumberOfCashFlows(); i++) {
      values[i][0] = _currencyAmountFormatter.formatCell(value.getNotionals().get(i), valueSpec, null);
      values[i][1] = _rateFormatter.formatCell(value.getFixedRates().get(i), valueSpec, null);
      values[i][2] = value.getAccrualStart().get(i).toString();
      values[i][3] = value.getAccrualEnd().get(i).toString();
      values[i][4] = _doubleFormatter.formatCell(value.getPaymentFractions().get(i), valueSpec, null);
      values[i][5] = _currencyAmountFormatter.formatCell(value.getPaymentAmounts().get(i), valueSpec, null);
      values[i][6] = _doubleFormatter.formatCell(value.getPaymentTimes().get(i), valueSpec, null);
      values[i][7] = _doubleFormatter.formatCell(value.getDiscountFactors().get(i), valueSpec, null);
      values[i][8] = _currencyAmountFormatter.formatCell(value.getDiscountedPaymentAmounts().get(i), valueSpec, null);
    }
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
