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
public class ExampleChConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleChConventions();
  /** GB holidays */
  private static final ExternalId CH = ExternalSchemes.financialRegionId("CH");

  /**
   * Restricted constructor.
   */
  protected ExampleChConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("CHF Deposit", ExternalIdBundle.of("CONVENTION", "CHF Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.CHF, CH);
    final DepositConvention overnightDeposit = new DepositConvention("CHF O/N Deposit", ExternalIdBundle.of("CONVENTION", "CHF O/N Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.CHF, CH);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "CHFLIBORP3M"), ExternalId.of("CONVENTION", "CHFLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("CHFLIBORP3M"), ExternalSchemes.syntheticSecurityId("CHFLIBORP6M"), ExternalId.of("CONVENTION", "CHF LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("CHF LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.CHF,
        LocalTime.of(11, 0), "US", CH, CH, "");
    // Overnight
    final OvernightIndexConvention overnight =
        new OvernightIndexConvention("TOISTOIS", ExternalIdBundle.of(ExternalId.of("CONVENTION", "TOISTOIS"), ExternalSchemes.syntheticSecurityId("TOISTOIS")),
        DayCounts.ACT_365, 0, Currency.CHF, CH);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("CHF OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "CHF OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.CHF)),
        Tenor.ONE_YEAR, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.CHF, CH, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("CHF OIS", ExternalId.of("CONVENTION", "CHF OIS").toBundle(),
        ExternalId.of("CONVENTION", "TOISTOIS"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING,
        0, false, StubType.SHORT_START, false, 1);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("CHF IBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "CHF IBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.CHF)),
        Tenor.SIX_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.CHF, CH, 0, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("CHF 6M IBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "CHF 6M IBOR"), ExternalSchemes.currencyRegionId(Currency.CHF)),
        ExternalId.of("CONVENTION", "CHFLIBORP6M"), false, LinearInterpolator1dAdapter.NAME, Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("CHF Government Bond",
        ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.CHF), CH), 0, 2,
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
