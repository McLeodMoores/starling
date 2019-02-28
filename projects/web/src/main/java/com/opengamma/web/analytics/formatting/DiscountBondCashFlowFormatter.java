/**
 *
 */
package com.opengamma.web.analytics.formatting;

import static com.mcleodmoores.financial.function.trade.DiscountBondCashFlows.DISCOUNTED_PAYMENT_AMOUNT;
import static com.mcleodmoores.financial.function.trade.DiscountBondCashFlows.DISCOUNT_FACTOR;
import static com.mcleodmoores.financial.function.trade.DiscountBondCashFlows.NOMINAL_PAYMENT_DATE;
import static com.mcleodmoores.financial.function.trade.DiscountBondCashFlows.NOTIONAL;
import static com.mcleodmoores.financial.function.trade.DiscountBondCashFlows.PAYMENT_TIME;

import java.util.HashMap;
import java.util.Map;

import com.mcleodmoores.financial.function.trade.DiscountBondCashFlows;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class DiscountBondCashFlowFormatter extends AbstractFormatter<DiscountBondCashFlows> {
  private static final String[] COLUMN_LABELS = { NOMINAL_PAYMENT_DATE, NOTIONAL, PAYMENT_TIME, DISCOUNT_FACTOR, DISCOUNTED_PAYMENT_AMOUNT};
  private static final String X_LABELS = "xLabels";
  private static final String Y_LABELS = "yLabels";
  private static final String MATRIX = "matrix";

  private final DoubleFormatter _doubleFormatter;
  private final CurrencyAmountFormatter _currencyAmountFormatter;

  /**
   * @param doubleFormatter  formats the discount factors
   * @param rateFormatter  formats the zero rates
   * @param currencyAmountFormatter  formats the currency amounts
   */
  DiscountBondCashFlowFormatter(final DoubleFormatter doubleFormatter, final RateFormatter rateFormatter,
      final CurrencyAmountFormatter currencyAmountFormatter) {
    super(DiscountBondCashFlows.class);
    _doubleFormatter = doubleFormatter;
    _currencyAmountFormatter = currencyAmountFormatter;
    addFormatter(new Formatter<DiscountBondCashFlows>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final DiscountBondCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final DiscountBondCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "DiscountBondCashFlows";
  }

  /**
   * Transforms the details object to an amount that can be displayed.
   * @param value  the FX forward details
   * @param valueSpec  the value specification
   * @return  the data
   */
  /* package */ Map<String, Object> formatExpanded(final DiscountBondCashFlows value, final ValueSpecification valueSpec) {
    final int columnCount = COLUMN_LABELS.length;
    final String[] yLabels = {""};
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[1][columnCount];
    values[0][0] = value.getMaturity();
    values[0][1] = _currencyAmountFormatter.formatCell(value.getNominalAmount(), valueSpec, null);
    values[0][2] = _doubleFormatter.formatCell(value.getPaymentTime(), valueSpec, null);
    values[0][3] = _doubleFormatter.formatCell(value.getDiscountFactor(), valueSpec, null);
    values[0][4] = _currencyAmountFormatter.formatCell(value.getDiscountedPaymentAmount(), valueSpec, null);
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
