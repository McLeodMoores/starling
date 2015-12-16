/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;

/**
 * Adapter for a Quandl financial convention visitor that by default returns the same value for all convention
 * types.
 * @param <T> The type of the return value
 */
public class QuandlFinancialConventionVisitorSameValueAdapter<T> implements QuandlFinancialConventionVisitor<T> {
  /** The result */
  private final T _result;

  /**
   * Creates an instance.
   * @param result  the result returned by all methods, can be null
   */
  public QuandlFinancialConventionVisitorSameValueAdapter(final T result) {
    _result = result;
  }

  @Override
  public T visitQuandlStirFutureConvention(final QuandlStirFutureConvention convention) {
    return _result;
  }

  @Override
  public T visitQuandlFedFundsFutureConvention(final QuandlFedFundsFutureConvention convention) {
    return _result;
  }

  @Override
  public T visitBondConvention(final BondConvention convention) {
    return _result;
  }

  @Override
  public T visitCMSLegConvention(final CMSLegConvention convention) {
    return _result;
  }

  @Override
  public T visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
    return _result;
  }

  @Override
  public T visitDeliverablePriceQuotedSwapFutureConvention(final DeliverablePriceQuotedSwapFutureConvention convention) {
    return _result;
  }

  @Override
  public T visitDepositConvention(final DepositConvention convention) {
    return _result;
  }

  @Override
  public T visitEquityConvention(final EquityConvention convention) {
    return _result;
  }

  @Override
  public T visitFXForwardAndSwapConvention(final FXForwardAndSwapConvention convention) {
    return _result;
  }

  @Override
  public T visitFXSpotConvention(final FXSpotConvention convention) {
    return _result;
  }

  @Override
  public T visitFederalFundsFutureConvention(final FederalFundsFutureConvention convention) {
    return _result;
  }

  @Override
  public T visitFixedInterestRateSwapLegConvention(final FixedInterestRateSwapLegConvention convention) {
    return _result;
  }

  @Override
  public T visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
    return _result;
  }

  @Override
  public T visitFloatingInterestRateSwapLegConvention(final FloatingInterestRateSwapLegConvention convention) {
    return _result;
  }

  @Override
  public T visitIMMFRAConvention(final RollDateFRAConvention convention) {
    return _result;
  }

  @Override
  public T visitIMMSwapConvention(final RollDateSwapConvention convention) {
    return _result;
  }

  @Override
  public T visitIborIndexConvention(final IborIndexConvention convention) {
    return _result;
  }

  @Override
  public T visitInflationLegConvention(final InflationLegConvention convention) {
    return _result;
  }

  @Override
  public T visitInterestRateFutureConvention(final InterestRateFutureConvention convention) {
    return _result;
  }

  @Override
  public T visitOISLegConvention(final OISLegConvention convention) {
    return _result;
  }

  @Override
  public T visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
    return _result;
  }

  @Override
  public T visitONCompoundedLegRollDateConvention(final ONCompoundedLegRollDateConvention convention) {
    return _result;
  }

  @Override
  public T visitOvernightIndexConvention(final OvernightIndexConvention convention) {
    return _result;
  }

  @Override
  public T visitPriceIndexConvention(final PriceIndexConvention convention) {
    return _result;
  }

  @Override
  public T visitSwapConvention(final SwapConvention convention) {
    return _result;
  }

  @Override
  public T visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
    return _result;
  }

  @Override
  public T visitSwapIndexConvention(final SwapIndexConvention convention) {
    return _result;
  }

  @Override
  public T visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
    return _result;
  }

  @Override
  public T visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
    return _result;
  }

}
