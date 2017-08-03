/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.HashMap;
import java.util.Map;

import com.mcleodmoores.financial.function.trade.FxForwardDetails;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */class FxForwardDetailsFormatter extends AbstractFormatter<FxForwardDetails> {
  private static final String[] COLUMN_LABELS = new String[] {"Pay Amount", "Receive Amount", "Payment Time",
      "Pay Discount Factor", "Receive Discount Factor", "Pay Zero Rate", "Receive Zero Rate"};
  private static final String X_LABELS = "xLabels";
  private static final String Y_LABELS = "yLabels";
  private static final String MATRIX = "matrix";

  private final DoubleFormatter _doubleFormatter;
  private final RateFormatter _rateFormatter;
  private final CurrencyAmountFormatter _currencyAmountFormatter;

  /**
   * @param doubleFormatter  formats the discount factors
   * @param rateFormatter  formats the zero rates
   * @param currencyAmountFormatter  formats the currency amounts
   */
  /* package */ FxForwardDetailsFormatter(final DoubleFormatter doubleFormatter, final RateFormatter rateFormatter,
      final CurrencyAmountFormatter currencyAmountFormatter) {
    super(FxForwardDetails.class);
    _doubleFormatter = doubleFormatter;
    _currencyAmountFormatter = currencyAmountFormatter;
    _rateFormatter = rateFormatter;
    addFormatter(new Formatter<FxForwardDetails>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final FxForwardDetails value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final FxForwardDetails value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "FxForwardDetails";
  }

  /**
   * Transforms the details object to an amount that can be displayed.
   * @param value  the FX forward details
   * @param valueSpec  the value specification
   * @return  the data
   */
  /* package */ Map<String, Object> formatExpanded(final FxForwardDetails value, final ValueSpecification valueSpec) {
    final int columnCount = 7;
    final String[] yLabels = { "" };
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[1][columnCount];
    values[0][0] = _currencyAmountFormatter.formatCell(value.getPayAmount(), valueSpec, null);
    values[0][1] = _currencyAmountFormatter.formatCell(value.getReceiveAmount(), valueSpec, null);
    values[0][2] = _doubleFormatter.formatCell(value.getPaymentTime(), valueSpec, null);
    values[0][3] = _doubleFormatter.formatCell(value.getPayDiscountFactor(), valueSpec, null);
    values[0][4] = _doubleFormatter.formatCell(value.getReceiveDiscountFactor(), valueSpec, null);
    values[0][5] = _rateFormatter.formatCell(value.getPayZeroRate(), valueSpec, null);
    values[0][6] = _rateFormatter.formatCell(value.getReceiveZeroRate(), valueSpec, null);
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
