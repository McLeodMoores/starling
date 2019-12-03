/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.convention;

import java.util.HashMap;
import java.util.Map;

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
    final Map<String, String> temp = new HashMap<>();
    temp.put("attr1", "val1");
    temp.put("attr2", "val2");
    // Deposit
    final DepositConvention deposit = new DepositConvention("JPY Deposit", ExternalIdBundle.of("CONVENTION", "JPY Deposit"), DayCounts.ACT_365,
        BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.JPY, JP);
    final DepositConvention overnightDeposit = new DepositConvention("JPY O/N Deposit", ExternalIdBundle.of("CONVENTION", "JPY O/N Deposit"), DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, 0, false, Currency.JPY, JP);
    // IBOR
    final ExternalIdBundle iborIds = ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPYLIBORP3M"), ExternalId.of("CONVENTION", "JPYLIBORP6M"),
        ExternalSchemes.syntheticSecurityId("JPYLIBORP3M"), ExternalSchemes.syntheticSecurityId("JPYLIBORP6M"), ExternalId.of("CONVENTION", "JPY LIBOR"));
    final IborIndexConvention ibor = new IborIndexConvention("JPY LIBOR", iborIds, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 0, false, Currency.JPY,
        LocalTime.of(11, 0), "US", JP, JP, "");
    ibor.setAttributes(temp);
    // Overnight
    final OvernightIndexConvention overnight = new OvernightIndexConvention("TONAR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "TONAR"), ExternalSchemes.syntheticSecurityId("TONAR")), DayCounts.ACT_365, 0, Currency.JPY, JP);
    // OIS
    final SwapFixedLegConvention oisFixedLeg = new SwapFixedLegConvention("JPY OIS Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY OIS Fixed"), ExternalSchemes.currencyRegionId(Currency.JPY)), Tenor.ONE_YEAR, DayCounts.ACT_365,
        BusinessDayConventions.MODIFIED_FOLLOWING, Currency.JPY, JP, 1, true, StubType.SHORT_START, false, 0);
    final OISLegConvention oisLeg = new OISLegConvention("JPY OIS TONAR", ExternalId.of("CONVENTION", "JPY OIS TONAR").toBundle(),
        ExternalId.of("CONVENTION", "TONAR"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 0, false, StubType.SHORT_START, false, 1);
    // IBOR Swaps
    final SwapFixedLegConvention iborFixedLeg = new SwapFixedLegConvention("JPY LIBOR Fixed",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY LIBOR Fixed"), ExternalSchemes.currencyRegionId(Currency.JPY)), Tenor.SIX_MONTHS,
        DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.JPY, JP, 0, true, StubType.SHORT_START, false, 0);
    final VanillaIborLegConvention ibor6mLeg = new VanillaIborLegConvention("JPY 6M LIBOR",
        ExternalIdBundle.of(ExternalId.of("CONVENTION", "JPY 6M LIBOR"), ExternalSchemes.currencyRegionId(Currency.JPY)),
        ExternalId.of("CONVENTION", "JPYLIBORP6M"), false, InterpolationMethod.NONE.name(), Tenor.SIX_MONTHS, 0, false, StubType.SHORT_START, false, 0);

    // Bond conventions
    final BondConvention bondConvention = new BondConvention("JPY Government Bond", ExternalIdBundle.of(ExternalSchemes.currencyRegionId(Currency.JPY), JP), 0,
        2, BusinessDayConventions.FOLLOWING, true, true);

    // Swap conventions
    final SwapConvention vanillaSwap = new SwapConvention("JPY Fixed/6M LIBOR", ExternalIdBundle.of("CONVENTION", "JPY Fixed/6M LIBOR"),
        iborFixedLeg.getExternalIdBundle().iterator().next(), ibor6mLeg.getExternalIdBundle().iterator().next());
    final SwapConvention oisSwap = new SwapConvention("JPY OIS", ExternalIdBundle.of("CONVENTION", "JPY OIS"),
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
