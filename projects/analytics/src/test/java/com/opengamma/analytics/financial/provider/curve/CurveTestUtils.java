/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorSameValueAdapter;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;

/**
 *
 */
public class CurveTestUtils {

  public static final InstrumentDefinitionVisitor<Void, Double> RATES_INITIALIZATION = new InitialGuessForRates();
  public static final InstrumentDefinitionVisitor<Void, Double> INFLATION_INITIALIZATION = new InitialGuessForInflation();

  protected static class InitialGuessForRates extends InstrumentDefinitionVisitorSameValueAdapter<Void, Double> {

    protected InitialGuessForRates() {
      super(0.01);
    }

    @Override
    public Double visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return swap.getFixedLeg().getNthPayment(0).getRate();
    }

    @Override
    public Double visitSwapDefinition(final SwapDefinition swap) {
      if (swap instanceof SwapFixedONDefinition) {
        return ((SwapFixedONDefinition) swap).getFixedLeg().getNthPayment(0).getRate();
      }
      if (swap instanceof SwapFixedCompoundedONCompoundedDefinition) {
        return ((SwapFixedCompoundedONCompoundedDefinition) swap).getFixedLeg().getNthPayment(0).getRate();
      }
      throw new IllegalStateException("Swaps of type " + swap.getClass() + " not supported");
    }

    @Override
    public Double visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return fra.getRate();
    }

    @Override
    public Double visitCashDefinition(final CashDefinition cash) {
      return cash.getRate();
    }

    @Override
    public Double visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition irFuture) {
      return 1 - irFuture.getTradePrice();
    }

    //TODO instrument types
  }

  protected static class InitialGuessForInflation extends InstrumentDefinitionVisitorSameValueAdapter<Void, Double> {

    protected InitialGuessForInflation() {
      super(100.);
    }

    @Override
    public Double visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return swap.getFixedLeg().getNthPayment(0).getRate();
    }

    @Override
    public Double visitSwapDefinition(final SwapDefinition swap) {
      if (swap instanceof SwapFixedONDefinition) {
        return ((SwapFixedONDefinition) swap).getFixedLeg().getNthPayment(0).getRate();
      }
      if (swap instanceof SwapFixedInflationZeroCouponDefinition) {
        if (((SwapFixedInflationZeroCouponDefinition) swap).getFirstLeg().getNthPayment(0) instanceof CouponInflationZeroCouponMonthlyDefinition) {
          return 100.;
        }
        if (((SwapFixedInflationZeroCouponDefinition) swap).getFirstLeg().getNthPayment(0) instanceof CouponInflationZeroCouponInterpolationDefinition) {
          return 100.;
        }
        return 100.;
      }
      throw new IllegalStateException("Swaps of type " + swap.getClass() + " not supported");
    }

    @Override
    public Double visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return fra.getRate();
    }

    @Override
    public Double visitCashDefinition(final CashDefinition cash) {
      return cash.getRate();
    }

  }

}
