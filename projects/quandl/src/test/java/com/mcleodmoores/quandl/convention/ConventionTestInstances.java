/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FinancialConvention;
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
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Test instances of {@link com.opengamma.core.convention.Convention}.
 */
public final class ConventionTestInstances {
  /** The convention scheme name */
  private static final String CONVENTION_SCHEME = "CONVENTION";
  /** Generates unit ids */
  private static final AtomicLong ID_GENERATOR = new AtomicLong();

  /**
   * A bond convention instance.
   */
  public static final BondConvention BOND = new BondConvention("USD BOND", ExternalIdBundle.of(CONVENTION_SCHEME, "USD BOND"),
      0, 2, BusinessDayConventions.FOLLOWING, false, false);

  /**
   * A CMS leg convention instance.
   */
  public static final CMSLegConvention CMS_LEG = new CMSLegConvention("USD CMS LEG", ExternalIdBundle.of(CONVENTION_SCHEME, "USD CMS LEG"),
      ExternalId.of(CONVENTION_SCHEME, "USD SWAP INDEX"), Tenor.ONE_YEAR, false);

  /**
   * A compounding ibor leg convention instance.
   */
  public static final CompoundingIborLegConvention COMPOUNDING_IBOR_LEG = new CompoundingIborLegConvention("USD COMPOUNDING IBOR LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD COMPOUNDING IBOR LEG"), ExternalId.of(CONVENTION_SCHEME, "USD IBOR"), Tenor.ONE_YEAR,
      CompoundingType.FLAT_COMPOUNDING, Tenor.THREE_MONTHS, StubType.NONE, 2, false, StubType.NONE, false, 2);

  /**
   * A deliverable swap future convention instance.
   */
  public static final DeliverablePriceQuotedSwapFutureConvention DELIVERABLE_SWAP_FUTURE = new DeliverablePriceQuotedSwapFutureConvention("USD DSF",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD DSF"), ExternalId.of(CONVENTION_SCHEME, "IMM"), ExternalSchemes.countryRegionId(Country.US),
      ExternalId.of(CONVENTION_SCHEME, "USD SWAP"), 1000);

  /**
   * A deposit convention instance.
   */
  public static final DepositConvention DEPOSIT = new DepositConvention("USD DEPOSIT", ExternalIdBundle.of(CONVENTION_SCHEME, "USD DEPOSIT"),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, false, Currency.USD, ExternalSchemes.countryRegionId(Country.US));

  /**
   * An equity convention instance.
   */
  public static final EquityConvention EQUITY = new EquityConvention("USD EQUITY", ExternalIdBundle.of(CONVENTION_SCHEME, "USD EQUITY"), 7);

  /**
   * An FX forward convention instance.
   */
  public static final FXForwardAndSwapConvention FX_FORWARD = new FXForwardAndSwapConvention("USD/EUR FX FORWARD",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD/EUR FX FORWARD"), ExternalId.of(CONVENTION_SCHEME, "USD/EUR FX SPOT"), BusinessDayConventions.FOLLOWING,
      false, ExternalSchemes.countryRegionId(Country.US));

  /**
   * An FX spot convention instance.
   */
  public static final FXSpotConvention FX_SPOT = new FXSpotConvention("USD/EUR FX SPOT", ExternalIdBundle.of(CONVENTION_SCHEME, "USD/EUR FX SPOT"),
      2, ExternalSchemes.countryRegionId(Country.US));

  /**
   * A Fed fund funds future convention instance.
   */
  public static final FederalFundsFutureConvention FED_FUNDS_FUTURE = new FederalFundsFutureConvention("FED FUNDS",
      ExternalIdBundle.of(CONVENTION_SCHEME, "FED FUNDS"), ExternalId.of(CONVENTION_SCHEME, "FED FUNDS"),
      ExternalSchemes.countryRegionId(Country.US), ExternalId.of(CONVENTION_SCHEME, "USD OVERNIGHT"), 100000);

  /**
   * A fixed interest rate swap leg convention instance.
   */
  public static final FixedInterestRateSwapLegConvention FIXED_IR_SWAP_LEG = new FixedInterestRateSwapLegConvention("FIXED USD IR SWAP",
      ExternalIdBundle.of(CONVENTION_SCHEME, "FIXED USD IR SWAP"), Collections.singleton(ExternalSchemes.countryRegionId(Country.US)),
      Collections.singleton(ExternalSchemes.countryRegionId(Country.US)), Collections.singleton(ExternalSchemes.countryRegionId(Country.US)),
      BusinessDayConventions.MODIFIED_FOLLOWING, BusinessDayConventions.MODIFIED_FOLLOWING, BusinessDayConventions.MODIFIED_FOLLOWING,
      DayCounts.ACT_360, PeriodFrequency.SEMI_ANNUAL, PeriodFrequency.SEMI_ANNUAL, DateRelativeTo.START, false, 2, RollConvention.EOM,
      CompoundingMethod.NONE, 2);

  /**
   * A fixed swap leg roll date convention instance.
   */
  public static final FixedLegRollDateConvention FIXED_ROLL_SWAP_LEG = new FixedLegRollDateConvention("FIXED USD ROLL SWAP LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "FIXED USD ROLL SWAP LEG"), Tenor.SIX_MONTHS, DayCounts.ACT_360, Currency.USD,
      ExternalSchemes.countryRegionId(Country.US), StubType.NONE, false, 2);

  /**
   * A floating interest rate swap leg convention instance.
   */
  public static final FloatingInterestRateSwapLegConvention FLOATING_IR_SWAP_LEG = new FloatingInterestRateSwapLegConvention("FLOATING USD IR SWAP",
      ExternalIdBundle.of(CONVENTION_SCHEME, "FLOATING USD IR SWAP"), Collections.singleton(ExternalSchemes.countryRegionId(Country.US)),
      Collections.singleton(ExternalSchemes.countryRegionId(Country.US)), Collections.singleton(ExternalSchemes.countryRegionId(Country.US)),
      BusinessDayConventions.MODIFIED_FOLLOWING, BusinessDayConventions.MODIFIED_FOLLOWING, BusinessDayConventions.MODIFIED_FOLLOWING,
      DayCounts.ACT_360, PeriodFrequency.QUARTERLY, PeriodFrequency.QUARTERLY, DateRelativeTo.START, false, 2, RollConvention.EOM,
      CompoundingMethod.NONE, FloatingRateType.IBOR, Collections.singleton(ExternalSchemes.countryRegionId(Country.US)), BusinessDayConventions.FOLLOWING,
      OffsetType.BUSINESS, PeriodFrequency.QUARTERLY, Collections.singleton(ExternalSchemes.countryRegionId(Country.US)), BusinessDayConventions.FOLLOWING,
      DateRelativeTo.END, 2);

  /**
   * An ibor index convention instance.
   */
  public static final IborIndexConvention IBOR_INDEX = new IborIndexConvention("USD IBOR INDEX", ExternalIdBundle.of(CONVENTION_SCHEME, "USD IBOR INDEX"),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.countryRegionId(Country.US),
      ExternalSchemes.countryRegionId(Country.US), "Page");

  /**
   * An inflation leg convention instance.
   */
  public static final InflationLegConvention INFLATION_LEG = new InflationLegConvention("USD INFLATION LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD INFLATION LEG"), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_365,
      true, 1, 2, ExternalId.of(CONVENTION_SCHEME, "USD PRICE INDEX"));

  /**
   * An interest rate future convention instance.
   */
  public static final InterestRateFutureConvention STIR_FUTURE = new InterestRateFutureConvention("USD STIR FUTURE",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD STIR FUTURE"), ExternalId.of(CONVENTION_SCHEME, "IMM"), ExternalSchemes.countryRegionId(Country.US),
      ExternalId.of(CONVENTION_SCHEME, "USD IBOR INDEX"));

  /**
   * An OIS swap leg convention instance.
   */
  public static final OISLegConvention OIS_LEG = new OISLegConvention("USD OIS LEG", ExternalIdBundle.of(CONVENTION_SCHEME, "USD OIS LEG"),
      ExternalId.of(CONVENTION_SCHEME, "USD OVERNIGHT INDEX"), Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, true, StubType.NONE,
      false, 0);

  /**
   * An overnight arithmetic average swap leg convention instance.
   */
  public static final ONArithmeticAverageLegConvention OVERNIGHT_AVERAGE_LEG = new ONArithmeticAverageLegConvention("USD OVERNIGHT AVERAGE LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD OVERNIGHT AVERAGE LEG"), ExternalId.of(CONVENTION_SCHEME, "USD OVERNIGHT INDEX"), Tenor.ONE_YEAR,
      BusinessDayConventions.MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);

  /**
   * An overnight compounded roll date leg convention instance.
   */
  public static final ONCompoundedLegRollDateConvention OVERNIGHT_COMPOUNDED_ROLL_DATE_LEG =
      new ONCompoundedLegRollDateConvention("USD OVERNIGHT COMPOUNDED ROLL DATE", ExternalIdBundle.of(CONVENTION_SCHEME, "USD OVERNIGHT COMPOUNDED ROLL DATE"),
          ExternalId.of(CONVENTION_SCHEME, "USD OVERNIGHT INDEX"), Tenor.ONE_YEAR, StubType.NONE, false, 2);

  /**
   * An overnight index convention instance.
   */
  public static final OvernightIndexConvention OVERNIGHT_INDEX = new OvernightIndexConvention("USD OVERNIGHT INDEX",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD OVERNIGHT INDEX"), DayCounts.ACT_360, 1, Currency.USD, ExternalSchemes.countryRegionId(Country.US));

  /**
   * A price index convention instance.
   */
  public static final PriceIndexConvention PRICE_INDEX = new PriceIndexConvention("USD PRICE INDEX", ExternalIdBundle.of(CONVENTION_SCHEME, "USD PRICE INDEX"),
      Currency.USD, ExternalSchemes.countryRegionId(Country.US), ExternalId.of("DATA PROVIDER", "USD PRICE INDEX"));

  /**
   * A Quandl Fed funds future convention instance.
   */
  public static final QuandlFedFundsFutureConvention QUANDL_FED_FUNDS_FUTURE = new QuandlFedFundsFutureConvention("QUANDL FED FUNDS",
      ExternalIdBundle.of(CONVENTION_SCHEME, "QUANDL FED FUNDS"), "16:00", "America/Chicago", 2500, ExternalId.of(CONVENTION_SCHEME, "USD OVERNIGHT INDEX"),
      "ABC", "ABC");

  /**
   * A short-term USD interest rate future instance with 3m tenor for the future and underlying.
   */
  public static final QuandlStirFutureConvention QUANDL_USD_3M_3M_STIR_FUTURE = new QuandlStirFutureConvention("USD 3M/3M STIR",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD 3M/3M STIR"), Currency.USD, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
      "16:00", "America/Chicago", 2500, ExternalId.of(CONVENTION_SCHEME, "USD IBOR INDEX"), 3, DayOfWeek.WEDNESDAY.name(),
      "ABC", "ABC");

  /**
   * An IMM FRA convention instance.
   */
  public static final RollDateFRAConvention IMM_FRA = new RollDateFRAConvention("USD IMM FRA", ExternalIdBundle.of(CONVENTION_SCHEME, "USD IMM FRA"),
      ExternalId.of(CONVENTION_SCHEME, "USD IBOR"), ExternalId.of(CONVENTION_SCHEME, RollConvention.EOM.name()));

  /**
   * An IMM swap convention instance.
   */
  public static final RollDateSwapConvention IMM_SWAP = new RollDateSwapConvention("USD IMM SWAP", ExternalIdBundle.of(CONVENTION_SCHEME, "USD IMM SWAP"),
      ExternalId.of(CONVENTION_SCHEME, "USD FIXED SWAP LEG"), ExternalId.of(CONVENTION_SCHEME, "USD IMM IBOR SWAP LEG"),
      ExternalId.of(CONVENTION_SCHEME, RollConvention.EOM.name()));

  /**
   * A swap convention instance.
   */
  public static final SwapConvention SWAP = new SwapConvention("USD SWAP", ExternalIdBundle.of(CONVENTION_SCHEME, "USD SWAP"),
      ExternalId.of(CONVENTION_SCHEME, "USD FIXED SWAP LEG"), ExternalId.of(CONVENTION_SCHEME, "USD IBOR SWAP LEG"));

  /**
   * A swap fixed leg convention instance.
   */
  public static final SwapFixedLegConvention SWAP_FIXED_LEG = new SwapFixedLegConvention("USD FIXED SWAP LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD FIXED SWAP LEG"), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
      Currency.USD, ExternalSchemes.countryRegionId(Country.US), 2, true, StubType.NONE, false, 2);

  /**
   * A swap index convention instance.
   */
  public static final SwapIndexConvention SWAP_INDEX = new SwapIndexConvention("USD SWAP INDEX", ExternalIdBundle.of(CONVENTION_SCHEME, "USD SWAP INDEX"),
      LocalTime.of(11, 0), ExternalId.of(CONVENTION_SCHEME, "USD SWAP"));

  /**
   * A vanilla ibor swap leg convention instance.
   */
  public static final VanillaIborLegConvention IBOR_SWAP_LEG = new VanillaIborLegConvention("USD IBOR SWAP LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD IBOR SWAP LEG"), ExternalId.of(CONVENTION_SCHEME, "USD IBOR INDEX"), false, "Linear", Tenor.THREE_MONTHS, 2,
      true, StubType.NONE, false, 0);

  /**
   * A vanilla IMM swap leg convention instance.
   */
  public static final VanillaIborLegRollDateConvention IMM_IBOR_SWAP_LEG = new VanillaIborLegRollDateConvention("USD IMM IBOR SWAP LEG",
      ExternalIdBundle.of(CONVENTION_SCHEME, "USD IMM IBOR SWAP LEG"), ExternalId.of(CONVENTION_SCHEME, "USD IBOR INDEX"), false, Tenor.THREE_MONTHS,
      StubType.NONE, false, 0);

  /**
   * A set of all of the convention instances.
   */
  public static final Set<FinancialConvention> INSTANCES = new HashSet<>();
  static {
    final String scheme = "TestCnv";
    BOND.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(BOND);
    CMS_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(CMS_LEG);
    COMPOUNDING_IBOR_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(COMPOUNDING_IBOR_LEG);
    DELIVERABLE_SWAP_FUTURE.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(DELIVERABLE_SWAP_FUTURE);
    DEPOSIT.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(DEPOSIT);
    EQUITY.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(EQUITY);
    FX_FORWARD.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FX_FORWARD);
    FX_SPOT.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FX_SPOT);
    FED_FUNDS_FUTURE.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FED_FUNDS_FUTURE);
    FIXED_IR_SWAP_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FIXED_IR_SWAP_LEG);
    FIXED_ROLL_SWAP_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FIXED_ROLL_SWAP_LEG);
    FLOATING_IR_SWAP_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(FLOATING_IR_SWAP_LEG);
    IBOR_INDEX.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(IBOR_INDEX);
    INFLATION_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(INFLATION_LEG);
    STIR_FUTURE.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(STIR_FUTURE);
    OIS_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(OIS_LEG);
    OVERNIGHT_AVERAGE_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(OVERNIGHT_AVERAGE_LEG);
    OVERNIGHT_COMPOUNDED_ROLL_DATE_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(OVERNIGHT_COMPOUNDED_ROLL_DATE_LEG);
    OVERNIGHT_INDEX.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(OVERNIGHT_INDEX);
    PRICE_INDEX.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(PRICE_INDEX);
    QUANDL_FED_FUNDS_FUTURE.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(QUANDL_FED_FUNDS_FUTURE);
    QUANDL_USD_3M_3M_STIR_FUTURE.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(QUANDL_USD_3M_3M_STIR_FUTURE);
    IMM_FRA.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(IMM_FRA);
    IMM_SWAP.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(IMM_SWAP);
    SWAP.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(SWAP);
    SWAP_FIXED_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(SWAP_FIXED_LEG);
    SWAP_INDEX.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(SWAP_INDEX);
    IBOR_SWAP_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(IBOR_SWAP_LEG);
    IMM_IBOR_SWAP_LEG.setUniqueId(UniqueId.of(scheme, Long.toString(ID_GENERATOR.incrementAndGet())));
    INSTANCES.add(IMM_IBOR_SWAP_LEG);
  }

  /**
   * Private constructor.
   */
  private ConventionTestInstances() {
  }
}
