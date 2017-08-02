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
public class ExampleEuConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleEuConventions();
  /** GB holidays */
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");

  /**
   * Restricted constructor.
   */
  protected ExampleEuConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit
    final DepositConvention deposit = new DepositConvention("EUR Deposit", ExternalIdBundle.of("CONVENTION", "EUR Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.EUR, EU);
    final DepositConvention overnightDeposit = new DepositConvention("EUR O/N Deposit", ExternalIdBundle.of("CONVENTION", "EUR O/N Deposit"),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.EUR, EU);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "EURLIBORP3M"), ExternalId.of("CONVENTION", "EURLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("EURLIBORP3M"), ExternalSchemes.syntheticSecurityId("EURLIBORP6M"), ExternalId.of("CONVENTION", "EUR LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("EUR LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.EUR,
        LocalTime.of(11, 0), "US", EU, EU, "");
    // Overnight
    final OvernightIndexConvention overnight =
        new OvernightIndexConvention("EONIA", ExternalIdBundle.of(ExternalId.of("CONVENTION", "EONIA"), ExternalSchemes.syntheticSecurityId("EONIA")),
        DayCounts.ACT_365, 0, Currency.EUR, EU);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("EUR OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "EUR OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.EUR)),
        Tenor.ONE_YEAR, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR, EU, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("EUR OIS", ExternalId.of("CONVENTION", "EUR OIS").toBundle(),
        ExternalId.of("CONVENTION", "EONIA"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING,
        0, false, StubType.SHORT_START, false, 1);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("EUR IBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "EUR IBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.EUR)),
        Tenor.SIX_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR, EU, 0, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor3mLeg = new VanillaIborLegConvention("EUR 6M IBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "EUR 6M IBOR"), ExternalSchemes.currencyRegionId(Currency.EUR)),
        ExternalId.of("CONVENTION", "EURLIBORP6M"), false, LinearInterpolator1dAdapter.NAME, Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("EUR Government Bond",
        ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.EUR), EU), 0, 2,
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
