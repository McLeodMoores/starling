/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.convention;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.IsdaCreditCurveConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * US conventions.
 */
public class ExampleUsConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleUsConventions();
  /** US holidays */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** New York + London holidays */
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");

  /**
   * Restricted constructor.
   */
  protected ExampleUsConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("USD Deposit", ExternalIdBundle.of("CONVENTION", "USD Deposit"), DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
    final DepositConvention overnightDeposit = new DepositConvention("USD O/N Deposit", ExternalIdBundle.of("CONVENTION", "USD O/N Deposit"), DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, 0, false, Currency.USD, US);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "USDLIBORP3M"), ExternalId.of("CONVENTION", "USDLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("USDLIBORP3M"), ExternalSchemes.syntheticSecurityId("USDLIBORP6M"), ExternalId.of("CONVENTION", "USD LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("USD LIBOR", iborIds, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 0, false, Currency.USD,
        LocalTime.of(11, 0), "US", NYLON, US, "");
    // Overnight
    final OvernightIndexConvention overnight = new OvernightIndexConvention("USDFF",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "USDFF"), ExternalSchemes.syntheticSecurityId("USDFF")), DayCounts.ACT_360, 0, Currency.USD, US);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("USD OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.USD)), Tenor.ONE_YEAR, DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, US, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("USD OIS FED FUNDS", ExternalId.of("CONVENTION", "USD OIS FED FUNDS").toBundle(),
        ExternalId.of("CONVENTION", "USDFF"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 0, false, StubType.SHORT_START, false, 0);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("USD LIBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD LIBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.USD)), Tenor.SIX_MONTHS,
        DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, US, 1, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("USD 3M LIBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD 3M LIBOR"), ExternalSchemes.currencyRegionId(Currency.USD)),
        ExternalId.of("CONVENTION", "USDLIBORP3M"), false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("USD Government Bond", ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.USD), US), 0,
        2, BusinessDayConventions.FOLLOWING, true, true);

    // Swap conventions
    final SwapConvention vanillaSwap = new SwapConvention("USD Fixed/3M LIBOR", ExternalIdBundle.of("CONVENTION", "USD Fixed/3M LIBOR"),
        iborFixedLeg.getExternalIdBundle().iterator().next(), ibor3mLeg.getExternalIdBundle().iterator().next());
    final SwapConvention oisSwap = new SwapConvention("USD OIS", ExternalIdBundle.of("CONVENTION", "USD OIS"),
        oisFixedLeg.getExternalIdBundle().iterator().next(), oisLeg.getExternalIdBundle().iterator().next());

    final IsdaCreditCurveConvention cds = new IsdaCreditCurveConvention();
    cds.setAccrualDayCount(DayCounts.ACT_360);
    cds.setBusinessDayConvention(BusinessDayConventions.FOLLOWING);
    cds.setPayAccOnDefault(true);
    cds.setCashSettle(3);
    cds.setCouponInterval(Period.ofMonths(6));
    cds.setCurveDayCount(DayCounts.ACT_365);
    cds.setName("USD CDS");
    cds.setPayAccOnDefault(true);
    cds.setStepIn(1);
    cds.setStubType(StubType.SHORT_START);
    cds.setExternalIdBundle(ExternalId.of(Currency.OBJECT_SCHEME, "USD").toBundle());

    addConvention(master, deposit);
    addConvention(master, overnightDeposit);
    addConvention(master, ibor);
    addConvention(master, overnight);
    addConvention(master, oisFixedLeg);
    addConvention(master, oisLeg);
    addConvention(master, iborFixedLeg);
    addConvention(master, ibor3mLeg);
    addConvention(master, bondConvention);
    addConvention(master, vanillaSwap);
    addConvention(master, oisSwap);
    addConvention(master, cds);
  }

}
