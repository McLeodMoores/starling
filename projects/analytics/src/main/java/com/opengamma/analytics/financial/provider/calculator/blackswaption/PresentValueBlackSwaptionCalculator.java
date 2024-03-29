/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackSwaptionCalculator
    extends InstrumentDerivativeVisitorAdapter<BlackSwaptionFlatProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSwaptionCalculator INSTANCE = new PresentValueBlackSwaptionCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static PresentValueBlackSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackSwaptionCalculator() {
  }

  /** Pricing method for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /** Pricing method for cash-settled swaptions */
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWT_CASH = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption,
      final BlackSwaptionFlatProviderInterface black) {
    return METHOD_SWT_PHYS.presentValue(swaption, black);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption,
      final BlackSwaptionFlatProviderInterface black) {
    return METHOD_SWT_CASH.presentValue(swaption, black);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface curves) {
    return MultipleCurrencyAmount
        .of(SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod.getInstance().presentValue(swaption, curves));
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface curves) {
    return MultipleCurrencyAmount.of(SwaptionCashFixedCompoundedONCompoundedBlackMethod.getInstance().presentValue(swaption, curves));
  }

}
