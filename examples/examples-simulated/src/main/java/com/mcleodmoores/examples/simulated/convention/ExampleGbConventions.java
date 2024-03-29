/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.convention;

import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
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
 * GB conventions.
 */
public class ExampleGbConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleGbConventions();
  /** GB holidays */
  private static final ExternalId GB = ExternalSchemes.financialRegionId("GB");
  /** New York + London holidays */
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");

  /**
   * Restricted constructor.
   */
  protected ExampleGbConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("GBP Deposit", ExternalIdBundle.of("CONVENTION", "GBP Deposit"), DayCounts.ACT_365,
        BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.GBP, GB);
    final DepositConvention overnightDeposit = new DepositConvention("GBP O/N Deposit", ExternalIdBundle.of("CONVENTION", "GBP O/N Deposit"), DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, 0, false, Currency.GBP, GB);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "GBPLIBORP3M"), ExternalId.of("CONVENTION", "GBPLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("GBPLIBORP3M"), ExternalSchemes.syntheticSecurityId("GBPLIBORP6M"), ExternalId.of("CONVENTION", "GBP LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("GBP LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.GBP,
        LocalTime.of(11, 0), "US", NYLON, GB, "");
    // Overnight
    final OvernightIndexConvention overnight = new OvernightIndexConvention("SONIA",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "SONIA"), ExternalSchemes.syntheticSecurityId("SONIA")), DayCounts.ACT_365, 0, Currency.GBP, GB);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("GBP OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "GBP OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.GBP)), Tenor.ONE_YEAR, DayCounts.ACT_365,
        BusinessDayConventions.MODIFIED_FOLLOWING, Currency.GBP, GB, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("GBP OIS SONIA", ExternalId.of("CONVENTION", "GBP OIS SONIA").toBundle(),
        ExternalId.of("CONVENTION", "SONIA"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 0, false, StubType.SHORT_START, false, 1);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("GBP LIBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "GBP LIBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.GBP)), Tenor.SIX_MONTHS,
        DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.GBP, GB, 0, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor6mLeg = new VanillaIborLegConvention("GBP 6M LIBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "GBP 6M LIBOR"), ExternalSchemes.currencyRegionId(Currency.GBP)),
        ExternalId.of("CONVENTION", "GBPLIBORP6M"), false, InterpolationMethod.NONE.name(), Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("GBP Government Bond", ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.GBP), GB), 0,
        2, BusinessDayConventions.FOLLOWING, true, true);

    // Swap conventions
    final SwapConvention vanillaSwap = new SwapConvention("GBP Fixed/6M LIBOR", ExternalIdBundle.of("CONVENTION", "GBP Fixed/6M LIBOR"),
        iborFixedLeg.getExternalIdBundle().iterator().next(), ibor6mLeg.getExternalIdBundle().iterator().next());
    final SwapConvention oisSwap = new SwapConvention("GBP OIS", ExternalIdBundle.of("CONVENTION", "GBP OIS"),
        oisFixedLeg.getExternalIdBundle().iterator().next(), oisLeg.getExternalIdBundle().iterator().next());

    addConvention(master, deposit);
    addConvention(master, overnightDeposit);
    addConvention(master, ibor);
    addConvention(master, overnight);
    addConvention(master, oisFixedLeg);
    addConvention(master, oisLeg);
    addConvention(master, iborFixedLeg);
    addConvention(master, ibor6mLeg);
    addConvention(master, bondConvention);
    addConvention(master, vanillaSwap);
    addConvention(master, oisSwap);
  }

}
