/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.testutils;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Provides instances of {@link com.opengamma.financial.security.FinancialSecurity} that can be used in testing.
 */
public final class SecurityInstances {

  /**
   * Restricted constructor.
   */
  private SecurityInstances() {
  }

  /**
   * A cash security.
   */
  public static final CashSecurity CASH = new CashSecurity(Currency.AUD, ExternalSchemes.countryRegionId(Country.AU), DateUtils.getUTCDate(2015, 1, 1),
      DateUtils.getUTCDate(2015, 1, 15), DayCounts.ACT_360, 0.001, 10000.);
  /**
   * A continuous zero deposit security.
   */
  public static final ContinuousZeroDepositSecurity CONTINUOUS_ZERO_DEPOSIT = new ContinuousZeroDepositSecurity(Currency.BRL, DateUtils.getUTCDate(2015, 1, 1),
      DateUtils.getUTCDate(2016, 1, 1), 0.07, ExternalSchemes.countryRegionId(Country.BR));
  /**
   * A corporate bond security.
   */
  public static final CorporateBondSecurity CORPORATE_BOND = new CorporateBondSecurity("ES", "Bank", "ES", "Bank", Currency.EUR,
      SimpleYieldConvention.SPANISH_T_BILLS, new Expiry(DateUtils.getUTCDate(2020, 1, 1)), "FIXED_RATE", 0.05, PeriodFrequency.SEMI_ANNUAL, DayCounts.ACT_365,
      DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 7, 1), 100., 10000, 100, 100, 100, 100);
  /**
   * An equity variance swap security.
   */
  public static final EquityVarianceSwapSecurity EQUITY_VARIANCE_SWAP = new EquityVarianceSwapSecurity(ExternalSchemes.bloombergTickerSecurityId("AC Equity"),
      Currency.GBP, 100, 10000, false, 252, DateUtils.getUTCDate(2015, 1, 1), DateUtils.getUTCDate(2016, 1, 1), DateUtils.getUTCDate(2015, 1, 1),
      ExternalSchemes.countryRegionId(Country.GB), SimpleFrequency.CONTINUOUS);
  /**
   * A FRA security.
   */
  public static final FRASecurity FRA = new FRASecurity(Currency.CAD, ExternalSchemes.countryRegionId(Country.CA), DateUtils.getUTCDate(2015, 1, 1),
      DateUtils.getUTCDate(2015, 4, 1), 0.01, 10000, ExternalSchemes.bloombergTickerSecurityId("RCB003M Index"), DateUtils.getUTCDate(2015, 4, 1));
  /**
   * A government bond security.
   */
  public static final GovernmentBondSecurity GOVERNMENT_BOND = new GovernmentBondSecurity("ES", "Country", "ES", "Domestic", Currency.EUR,
      SimpleYieldConvention.SPANISH_T_BILLS, new Expiry(DateUtils.getUTCDate(2020, 1, 1)), "FIXED_RATE", 0.05, PeriodFrequency.SEMI_ANNUAL, DayCounts.ACT_365,
      DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 7, 1), 100., 10000, 100, 100, 100, 100);
  /**
   * An inflation bond security.
   */
  public static final InflationBondSecurity INFLATION_BOND = new InflationBondSecurity("ES", "Country", "ES", "Domestic Inflation", Currency.EUR,
      SimpleYieldConvention.SPANISH_T_BILLS, new Expiry(DateUtils.getUTCDate(2020, 1, 1)), "FIXED_RATE", 0.05, PeriodFrequency.SEMI_ANNUAL, DayCounts.ACT_365,
      DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 7, 1), 100., 10000, 100, 100, 100, 100);
  /**
   * A municipal bond security.
   */
  public static final MunicipalBondSecurity MUNICIPAL_BOND = new MunicipalBondSecurity("ES", "Madrid", "ES", "City", Currency.EUR,
      SimpleYieldConvention.SPANISH_T_BILLS, new Expiry(DateUtils.getUTCDate(2020, 1, 1)), "FIXED_RATE", 0.05, PeriodFrequency.SEMI_ANNUAL, DayCounts.ACT_365,
      DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 7, 1), 100., 10000, 100, 100, 100, 100);
  /**
   * A fixed interest rate swap leg.
   */
  public static final SwapLeg FIXED_SWAP_LEG = new FixedInterestRateLeg(DayCounts.ACT_360, PeriodFrequency.SEMI_ANNUAL,
      ExternalSchemes.countryRegionId(Country.US), BusinessDayConventions.MODIFIED_FOLLOWING, new InterestRateNotional(Currency.USD, 100000), false, 0.01);
  /**
   * A floating ibor swap leg.
   */
  public static final SwapLeg IBOR_SWAP_LEG = new FloatingInterestRateLeg(DayCounts.ACT_360, PeriodFrequency.SEMI_ANNUAL,
      ExternalSchemes.countryRegionId(Country.US), BusinessDayConventions.MODIFIED_FOLLOWING, new InterestRateNotional(Currency.USD, 100000), false,
      ExternalSchemes.bloombergTickerSecurityId("US0006M Index"), FloatingRateType.IBOR);
  /**
   * A vanilla ibor swap security.
   */
  public static final SwapSecurity VANILLA_IBOR_SWAP = new SwapSecurity(DateUtils.getUTCDate(2015, 1, 1), DateUtils.getUTCDate(2015, 1, 1),
      DateUtils.getUTCDate(2020, 1, 1), "OTHER", FIXED_SWAP_LEG, IBOR_SWAP_LEG);
  /**
   * The id for the vanilla ibor swap.
   */
  public static final ExternalId VANILLA_IBOR_SWAP_ID = ExternalId.of("sec", "VANILLA_IBOR_SWAP");
  static {
    VANILLA_IBOR_SWAP.setExternalIdBundle(VANILLA_IBOR_SWAP_ID.toBundle());
  }
  /**
   * A cross-currency swap security.
   */
  public static final SwapSecurity CROSS_CURRENCY_SWAP = new SwapSecurity(DateUtils.getUTCDate(2015, 1, 1), DateUtils.getUTCDate(2015, 1, 1),
      DateUtils.getUTCDate(2020, 1, 1), "OTHER", FIXED_SWAP_LEG,
      new FixedInterestRateLeg(DayCounts.ACT_360, PeriodFrequency.SEMI_ANNUAL,
          ExternalSchemes.countryRegionId(Country.MX), BusinessDayConventions.MODIFIED_FOLLOWING, new InterestRateNotional(Currency.MXN, 1200000),
          false, 0.05));
  /**
   * The id for the cross-currency swap.
   */
  public static final ExternalId CROSS_CURRENCY_SWAP_ID = ExternalId.of("sec", "CROSS_CURRENCY_SWAP");
  static {
    CROSS_CURRENCY_SWAP.setExternalIdBundle(CROSS_CURRENCY_SWAP_ID.toBundle());
  }
  /**
   * A bill security.
   */
  public static final BillSecurity BILL = new BillSecurity(Currency.NZD, new Expiry(DateUtils.getUTCDate(2016, 1, 1)), DateUtils.getUTCDate(2015, 1, 1), 100,
      2, ExternalSchemes.countryRegionId(Country.NZ), SimpleYieldConvention.DISCOUNT, DayCounts.ACT_360, ExternalId.of("le", Country.NZ.getCode()));
  /**
   * A swaption security.
   */
  public static final SwaptionSecurity VANILLA_IBOR_SWAPTION = new SwaptionSecurity(false, VANILLA_IBOR_SWAP_ID, true,
      new Expiry(DateUtils.getUTCDate(2016, 1, 1)), false, Currency.USD, 10000., new EuropeanExerciseType(), DateUtils.getUTCDate(2015, 1, 1));
  /**
   * A cross-currency swaption security.
   */
  public static final SwaptionSecurity CROSS_CURRENCY_SWAPTION = new SwaptionSecurity(false, CROSS_CURRENCY_SWAP_ID, true,
      new Expiry(DateUtils.getUTCDate(2016, 1, 1)), false, Currency.USD, 10000., new EuropeanExerciseType(), DateUtils.getUTCDate(2015, 1, 1));
}
