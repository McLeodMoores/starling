/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorSameValueAdapter;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 *
 */
public class CurveTestUtils {

  public static final InstrumentDefinitionVisitor<Void, Double> RATES_INITIALIZATION = new InitialGuessForRates();
  public static final InstrumentDefinitionVisitor<Void, Double> INFLATION_INITIALIZATION = new InitialGuessForInflation();
  public static final InstrumentDefinitionVisitor<Map<Index, ZonedDateTimeDoubleTimeSeries>, ZonedDateTimeDoubleTimeSeries[]> FIXING_TIME_SERIES_PROVIDER = new FixingTimeSeriesProvider();

  protected static class FixingTimeSeriesProvider extends InstrumentDefinitionVisitorSameValueAdapter<Map<Index, ZonedDateTimeDoubleTimeSeries>, ZonedDateTimeDoubleTimeSeries[]> {

    protected FixingTimeSeriesProvider() {
      super(new ZonedDateTimeDoubleTimeSeries[] {ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC()});
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries[] visitSwapDefinition(final SwapDefinition swap, final Map<Index, ZonedDateTimeDoubleTimeSeries> data) {
      final ZonedDateTimeDoubleTimeSeries[] firstLegTs = swap.getFirstLeg().accept(this, data);
      final ZonedDateTimeDoubleTimeSeries[] secondLegTs = swap.getSecondLeg().accept(this, data);
      final ZonedDateTimeDoubleTimeSeries[] result = new ZonedDateTimeDoubleTimeSeries[firstLegTs.length + secondLegTs.length];
      System.arraycopy(firstLegTs, 0, result, 0, firstLegTs.length);
      System.arraycopy(secondLegTs, 0, result, firstLegTs.length, secondLegTs.length);
      return result;
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries[] visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final Map<Index, ZonedDateTimeDoubleTimeSeries> data) {
      // overly cautious, as no annuities with different coupon types are used in the tests
      // linked hash-set used to preserve order
      final Set<ZonedDateTimeDoubleTimeSeries> tss = new LinkedHashSet<>();
      for (final PaymentDefinition payment : annuity.getPayments()) {
        final ZonedDateTimeDoubleTimeSeries[] tsForPayment = payment.accept(this, data);
        for (final ZonedDateTimeDoubleTimeSeries ts : tsForPayment) {
          tss.add(ts);
        }
      }
      return tss.toArray(new ZonedDateTimeDoubleTimeSeries[tss.size()]);
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries[] visitCouponIborDefinition(final CouponIborDefinition coupon, final Map<Index, ZonedDateTimeDoubleTimeSeries> data) {
      final ZonedDateTimeDoubleTimeSeries ts = data.get(coupon.getIndex());
      if (ts == null) {
        throw new IllegalStateException("Could not get fixing series for " + coupon.getIndex());
      }
      return new ZonedDateTimeDoubleTimeSeries[] {ts};
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries[] visitCouponOISDefinition(final CouponONDefinition coupon, final Map<Index, ZonedDateTimeDoubleTimeSeries> data) {
      final ZonedDateTimeDoubleTimeSeries ts = data.get(coupon.getIndex());
      if (ts == null) {
        throw new IllegalStateException("Could not get fixing series for " + coupon.getIndex());
      }
      return new ZonedDateTimeDoubleTimeSeries[] {ts};
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries[] visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final Map<Index, ZonedDateTimeDoubleTimeSeries> data) {
      final ZonedDateTimeDoubleTimeSeries ts = data.get(fra.getIndex());
      if (ts == null) {
        throw new IllegalStateException("Could not get fixing series for " + fra.getIndex());
      }
      return new ZonedDateTimeDoubleTimeSeries[] {ts};
    }
  }

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

  public static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final ZonedDateTime valuationDate) {
    final InstrumentDerivative ird;
    if (instrument instanceof InstrumentDefinitionWithData) {
      final ZonedDateTimeDoubleTimeSeries[] ts = instrument.accept(FIXING_TIME_SERIES_PROVIDER, fixingTs);
      if (instrument instanceof ForwardRateAgreementDefinition) {
        if (ts.length != 1) {
          throw new IllegalStateException();
        }
        ird = ((ForwardRateAgreementDefinition) instrument).toDerivative(valuationDate, ts[0]);
      } else if (instrument instanceof SwapDefinition) {
        ird = ((InstrumentDefinitionWithData<InstrumentDerivative, Object>) instrument).toDerivative(valuationDate, ts);
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      ird = instrument.toDerivative(valuationDate);
    }
    return ird;
  }

  public static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final ZonedDateTime valuationDate) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int i = 0; i < definitions.length; i++) {
      instruments[i] = new InstrumentDerivative[definitions[i].length];
      int j = 0;
      for (final InstrumentDefinition<?> instrument : definitions[i]) {
        instruments[i][j++] = convert(instrument, fixingTs, valuationDate);
      }
    }
    return instruments;
  }

}
