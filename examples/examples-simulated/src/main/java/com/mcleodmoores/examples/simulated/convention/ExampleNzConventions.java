/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.convention;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.core.id.ExternalSchemes;
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
 *
 */
public final class ExampleNzConventions extends ConventionMasterInitializer {

  /**
   * An instance.
   */
  public static final ConventionMasterInitializer INSTANCE = new ExampleNzConventions();

  private static final ExternalId NZ = ExternalSchemes.financialRegionId("NZ");
  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("NZD Deposit", ExternalIdBundle.of("CONVENTION", "NZD Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.NZD, NZ);
    final DepositConvention overnightDeposit = new DepositConvention("NZD O/N Deposit", ExternalIdBundle.of("CONVENTION", "NZD O/N Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.NZD, NZ);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "NZDLIBORP3M"), ExternalId.of("CONVENTION", "NZDLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("NZDLIBORP3M"), ExternalSchemes.syntheticSecurityId("NZDLIBORP6M"));
    final IborIndexConvention ibor = new IborIndexConvention("NZD LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.NZD,
        LocalTime.of(11, 0), "NZ", NZ, NZ, "");
    // Overnight
    final OvernightIndexConvention overnight = new OvernightIndexConvention("NZD O/N",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "NZDON"), ExternalSchemes.syntheticSecurityId("NZDON")), DayCounts.ACT_365, 0, Currency.NZD, NZ);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("NZD OIS Fixed", ExternalIdBundle.of("CONVENTION", "NZD OIS Fixed"),
        Tenor.ONE_YEAR, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.NZD, NZ, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("NZD OIS", ExternalId.of("CONVENTION", "NZD OIS").toBundle(),
        ExternalId.of("CONVENTION", "NZDON"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING,
        0, false, StubType.SHORT_START, false, 0);
    // IBOR Swaps
    final SwapFixedLegConvention ibor3mFixedLeg = new SwapFixedLegConvention("NZD 3M", ExternalIdBundle.of("CONVENTION", "NZD 3M"),
        Tenor.THREE_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.NZD, NZ, 1, true, StubType.SHORT_START, false, 0);
    final SwapFixedLegConvention ibor6mFixedLeg = new SwapFixedLegConvention("NZD 6M", ExternalIdBundle.of("CONVENTION", "NZD 6M"),
        Tenor.SIX_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.NZD, NZ, 1, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("NZD 3M", ExternalIdBundle.of("CONVENTION", "NZD 3M"),
        ExternalId.of("CONVENTION", "NZDLIBORP3M"), false, LinearInterpolator1dAdapter.NAME, Tenor.THREE_MONTHS, 0, false, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor6mLeg = new VanillaIborLegConvention("NZD 6M", ExternalIdBundle.of("CONVENTION", "NZD 6M"),
        ExternalId.of("CONVENTION", "NZDLIBORP6M"), false, LinearInterpolator1dAdapter.NAME, Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    addConvention(master, deposit);
    addConvention(master, overnightDeposit);
    addConvention(master, ibor);
    addConvention(master, overnight);
    addConvention(master, oisFixedLeg);
    addConvention(master, oisLeg);
    addConvention(master, ibor3mFixedLeg);
    addConvention(master, ibor6mFixedLeg);
    addConvention(master, ibor3mLeg);
    addConvention(master, ibor6mLeg);
  }

  private ExampleNzConventions() {
  }
}
