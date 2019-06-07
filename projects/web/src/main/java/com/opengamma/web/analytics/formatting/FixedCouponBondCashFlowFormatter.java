/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.ACCRUAL_YEAR_FRACTION;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.COUPON_RATE;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.DISCOUNTED_PAYMENT_AMOUNT;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.DISCOUNT_FACTOR;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.END_ACCRUAL_DATES;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.NOMINAL_PAYMENT_DATES;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.NOTIONAL;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.PAYMENT_AMOUNT;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.PAYMENT_TIME;
import static com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows.START_ACCRUAL_DATES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.financial.function.trade.FixedCouponBondCashFlows;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FixedCouponBondCashFlowFormatter extends AbstractFormatter<FixedCouponBondCashFlows> {
  private static final String[] COLUMN_LABELS = { NOTIONAL, COUPON_RATE, START_ACCRUAL_DATES, END_ACCRUAL_DATES, NOMINAL_PAYMENT_DATES, ACCRUAL_YEAR_FRACTION,
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
  FixedCouponBondCashFlowFormatter(final DoubleFormatter doubleFormatter, final RateFormatter rateFormatter,
      final CurrencyAmountFormatter currencyAmountFormatter) {
    super(FixedCouponBondCashFlows.class);
    _doubleFormatter = doubleFormatter;
    _currencyAmountFormatter = currencyAmountFormatter;
    _rateFormatter = rateFormatter;
    addFormatter(new Formatter<FixedCouponBondCashFlows>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final FixedCouponBondCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final FixedCouponBondCashFlows value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "FixedCouponBondCashFlows";
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
  /* package */ Map<String, Object> formatExpanded(final FixedCouponBondCashFlows value, final ValueSpecification valueSpec) {
    final int columnCount = COLUMN_LABELS.length;
    final String[] yLabels = new String[value.getNumberOfCashFlows()];
    Arrays.fill(yLabels, "");
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[value.getNumberOfCashFlows()][columnCount];
    for (int i = 0; i < value.getNumberOfCashFlows(); i++) {
      final LocalDate accrualStartDate = value.getAccrualStart().get(i);
      final LocalDate accrualEndDate = value.getAccrualEnd().get(i);
      final CurrencyAmount notional = value.getNotionals().get(i);
      final Double coupon = value.getCouponRates().get(i);
      final LocalDate nominalPaymentDate = value.getNominalPaymentDates().get(i);
      final Double accrualFraction = value.getAccrualFractions().get(i);
      final CurrencyAmount paymentAmount = value.getPaymentAmounts().get(i);
      final Double paymentTime = value.getPaymentTimes().get(i);
      final Double discountFactor = value.getDiscountFactors().get(i);
      final List<CurrencyAmount> discountedPayment = value.getDiscountedPaymentAmounts();
      values[i][0] = notional == null ? "-" : _currencyAmountFormatter.formatCell(notional, valueSpec, null);
      values[i][1] = coupon == null ? "-" : _rateFormatter.formatCell(coupon, valueSpec, null);
      values[i][2] = accrualStartDate == null ? "-" : accrualStartDate.toString();
      values[i][3] = accrualEndDate == null ? "-" : accrualEndDate.toString();
      values[i][4] = nominalPaymentDate == null ? "-" : nominalPaymentDate.toString();
      values[i][5] = accrualFraction == null ? "-" : _doubleFormatter.formatCell(accrualFraction, valueSpec, null);
      values[i][6] = _currencyAmountFormatter.formatCell(paymentAmount, valueSpec, null);
      values[i][7] = _doubleFormatter.formatCell(paymentTime, valueSpec, null);
      values[i][8] = _doubleFormatter.formatCell(discountFactor, valueSpec, null);
      values[i][9] = _currencyAmountFormatter.formatCell(discountedPayment.get(i), valueSpec, null);
    }
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
