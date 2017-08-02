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
public final class ExampleAuConventions extends ConventionMasterInitializer {

  /**
   * An instance.
   */
  public static final ConventionMasterInitializer INSTANCE = new ExampleAuConventions();

  private static final ExternalId AU = ExternalSchemes.financialRegionId("AU");
  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("AUD Deposit", ExternalIdBundle.of("CONVENTION", "AUD Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.AUD, AU);
    final DepositConvention overnightDeposit = new DepositConvention("AUD O/N Deposit", ExternalIdBundle.of("CONVENTION", "AUD O/N Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.AUD, AU);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "AUDLIBORP3M"), ExternalId.of("CONVENTION", "AUDLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("AUDLIBORP3M"), ExternalSchemes.syntheticSecurityId("AUDLIBORP6M"), ExternalId.of("CONVENTION", "AUD LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("AUD LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.AUD,
        LocalTime.of(11, 0), "AU", AU, AU, "");
    // Overnight
    final OvernightIndexConvention overnight = new OvernightIndexConvention("AUD O/N",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "AUDON"), ExternalSchemes.syntheticSecurityId("AUDON")), DayCounts.ACT_365, 0, Currency.AUD, AU);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("AUD OIS Fixed", ExternalIdBundle.of("CONVENTION", "AUD"),
        Tenor.ONE_YEAR, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.AUD, AU, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("AUD OIS Fixed", ExternalId.of("CONVENTION", "AUD OIS").toBundle(),
        ExternalId.of("CONVENTION", "AUDON"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING,
        0, false, StubType.SHORT_START, false, 0);
    // IBOR Swaps
    final SwapFixedLegConvention ibor3mFixedLeg = new SwapFixedLegConvention("AUD 3M IBOR Fixed", ExternalIdBundle.of("CONVENTION", "AUD 3M IBOR Fixed"),
        Tenor.THREE_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.AUD, AU, 1, true, StubType.SHORT_START, false, 0);
    final SwapFixedLegConvention ibor6mFixedLeg = new SwapFixedLegConvention("AUD 6M IBOR Fixed", ExternalIdBundle.of("CONVENTION", "AUD 6M IBOR Fixed"),
        Tenor.SIX_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.AUD, AU, 1, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("AUD 3M IBOR", ExternalIdBundle.of("CONVENTION", "AUD 3M IBOR"),
        ExternalId.of("CONVENTION", "AUDLIBORP3M"), false, LinearInterpolator1dAdapter.NAME, Tenor.THREE_MONTHS, 0, false, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor6mLeg = new VanillaIborLegConvention("AUD 6M IBOR", ExternalIdBundle.of("CONVENTION", "AUD 6M IBOR"),
        ExternalId.of("CONVENTION", "AUDLIBORP6M"), false, LinearInterpolator1dAdapter.NAME, Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

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

  private ExampleAuConventions() {
  }
}
