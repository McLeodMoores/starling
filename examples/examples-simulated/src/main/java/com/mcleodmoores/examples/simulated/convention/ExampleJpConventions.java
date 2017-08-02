/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.convention;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * GB conventions.
 */
public class ExampleJpConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleJpConventions();
  /** GB holidays */
  private static final ExternalId JP = ExternalSchemes.financialRegionId("JP");

  /**
   * Restricted constructor.
   */
  protected ExampleJpConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("JPY Deposit", ExternalIdBundle.of("CONVENTION", "JPY Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.JPY, JP);
    final DepositConvention overnightDeposit = new DepositConvention("JPY O/N Deposit", ExternalIdBundle.of("CONVENTION", "JPY O/N Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.JPY, JP);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPYLIBORP3M"), ExternalId.of("CONVENTION", "JPYLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("JPYLIBORP3M"), ExternalSchemes.syntheticSecurityId("JPYLIBORP6M"), ExternalId.of("CONVENTION", "JPY LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("JPY LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.JPY,
        LocalTime.of(11, 0), "US", JP, JP, "");
    // Overnight
    final OvernightIndexConvention overnight =
        new OvernightIndexConvention("TONAR", ExternalIdBundle.of(ExternalId.of("CONVENTION", "TONAR"), ExternalSchemes.syntheticSecurityId("TONAR")),
        DayCounts.ACT_365, 0, Currency.JPY, JP);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("JPY OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.JPY)),
        Tenor.ONE_YEAR, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.JPY, JP, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("JPY OIS", ExternalId.of("CONVENTION", "JPY OIS").toBundle(),
        ExternalId.of("CONVENTION", "TONAR"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING,
        0, false, StubType.SHORT_START, false, 1);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("JPY IBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY IBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.JPY)),
        Tenor.SIX_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.JPY, JP, 0, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("JPY 6M IBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY 6M IBOR"), ExternalSchemes.currencyRegionId(Currency.JPY)),
        ExternalId.of("CONVENTION", "JPYLIBORP6M"), false, LinearInterpolator1dAdapter.NAME, Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("JPY Government Bond",
        ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.JPY), JP), 0, 2,
        BusinessDayConventions.FOLLOWING, true, true);

    addConvention(master, deposit);
    addConvention(master, overnightDeposit);
    addConvention(master, ibor);
    addConvention(master, overnight);
    addConvention(master, oisFixedLeg);
    addConvention(master, oisLeg);
    addConvention(master, iborFixedLeg);
    addConvention(master, ibor3mLeg);
    addConvention(master, bondConvention);
  }

}
