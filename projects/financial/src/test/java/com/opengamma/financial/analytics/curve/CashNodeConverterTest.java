/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemoryHolidaySource;
import com.opengamma.engine.InMemoryRegionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CashNodeConverter}.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class CashNodeConverterTest {
  /** The curve node id mapper name */
  private static final String MAPPER = "Mapper";
  /** A working day calendar containing only weekends */
  private static final SimpleHoliday WEEKEND_ONLY_HOLIDAYS = new SimpleHolidayWithWeekend(Collections.<LocalDate>emptySet(), WeekendType.SATURDAY_SUNDAY);
  /** The scheme */
  private static final String SCHEME = "Test";
  /** US region */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** One day deposit convention id */
  private static final ExternalId DEPOSIT_1D_ID = ExternalId.of(SCHEME, "USD 1d Deposit");
  /** One month deposit convention id */
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  /** Overnight convention name */
  private static final String USD_OVERNIGHT_CONVENTION_NAME = "USD Overnight";
  /** Overnight convention id */
  private static final ExternalId USD_OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, USD_OVERNIGHT_CONVENTION_NAME);
  /** Act/360 Libor convention name */
  private static final String USDLIBOR_ACT_360_CONVENTION_NAME = "USD Libor ACT/360";
  /** Act/360 Libor convention id */
  private static final ExternalId USDLIBOR_ACT_360_CONVENTION_ID = ExternalId.of(SCHEME, USDLIBOR_ACT_360_CONVENTION_NAME);
  /** 30/360 Libor convention name */
  private static final String USDLIBOR_30_360_CONVENTION_NAME = "USD Libor 30/360";
  /** 30/360 Libor convention id */
  private static final ExternalId USDLIBOR_30_360_ID = ExternalId.of(SCHEME, USDLIBOR_30_360_CONVENTION_NAME);
  /** 3M LIBOR index name */
  private static final String USDLIBOR3M_NAME = "USDLIBOR3M";
  /** 3M LIBOR index ticker */
  private static final ExternalId USDLIBOR3M_ID = ExternalSchemes.bloombergTickerSecurityId("US0003M Index");
  /** 3M LIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR3M =
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR3M_NAME, "ICE LIBOR 3M - USD", Tenor.THREE_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  /** 6M LIBOR index name */
  private static final String USDLIBOR6M_NAME = "USDLIBOR6M";
  /** 6M LIBOR index ticker */
  private static final ExternalId USDLIBOR6M_ID = ExternalSchemes.bloombergTickerSecurityId("US0006M Index");
  /** 6M LIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR6M =
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR6M_NAME, "ICE LIBOR 6M - USD", Tenor.SIX_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  /** Fed fund index name */
  private static final String USD_FEDFUND_INDEX_NAME = "Fed Funds Effective Rate";
  /** Fed fund ticker */
  private static final ExternalId USD_FEDFUND_INDEX_ID = ExternalSchemes.bloombergTickerSecurityId("FEDL1 Index");
  /** Fed fund index security */
  private static final OvernightIndex USD_FEDFUND_INDEX = new OvernightIndex(USD_FEDFUND_INDEX_NAME, USD_OVERNIGHT_CONVENTION_ID);
  /** Overnight index convention */
  private static final OvernightIndexConvention USD_OVERNIGHT_CONVENTION = new OvernightIndexConvention(USD_OVERNIGHT_CONVENTION_NAME,
      USD_OVERNIGHT_CONVENTION_ID.toBundle(), DayCounts.ACT_360, 1, Currency.USD, US);
  /** One day deposit convention */
  private static final DepositConvention DEPOSIT_1D = new DepositConvention("USD 1d Deposit", ExternalIdBundle.of(DEPOSIT_1D_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 0, false, Currency.USD, US);
  /** One month deposit convention */
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", ExternalIdBundle.of(DEPOSIT_1M_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  /** Act/360 Libor convention */
  private static final IborIndexConvention USDLIBOR_ACT_360 =
      new IborIndexConvention(USDLIBOR_ACT_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_ACT_360_CONVENTION_ID),
          DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  /** 30/360 Libor convention */
  private static final IborIndexConvention LIBOR_30_360 = new IborIndexConvention(USDLIBOR_30_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_30_360_ID),
      DayCounts.THIRTY_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  /** A security source */
  private static final InMemorySecuritySource SECURITY_SOURCE = new InMemorySecuritySource();
  /** A convention source */
  private static final InMemoryConventionSource CONVENTION_SOURCE = new InMemoryConventionSource();
  /** A holiday source */
  private static final InMemoryHolidaySource HOLIDAY_SOURCE = new InMemoryHolidaySource();
  /** A region source */
  private static final InMemoryRegionSource REGION_SOURCE = new InMemoryRegionSource();
  /** The valuation date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 5, 1);
  /** The market data id */
  private static final ExternalId MARKET_DATA_ID = ExternalId.of(SCHEME, "Cash");
  /** The rate */
  private static final double RATE = 0.0012345;
  /** The market data bundle */
  private static final SnapshotDataBundle MARKET_VALUES = new SnapshotDataBundle();

  static {
    USDLIBOR3M.addExternalId(USDLIBOR3M_ID);
    USDLIBOR6M.addExternalId(USDLIBOR6M_ID);
    USD_FEDFUND_INDEX.addExternalId(USD_FEDFUND_INDEX_ID);

    CONVENTION_SOURCE.addConvention(DEPOSIT_1D);
    CONVENTION_SOURCE.addConvention(DEPOSIT_1M);
    CONVENTION_SOURCE.addConvention(USDLIBOR_ACT_360);
    CONVENTION_SOURCE.addConvention(LIBOR_30_360);
    CONVENTION_SOURCE.addConvention(USD_OVERNIGHT_CONVENTION);

    HOLIDAY_SOURCE.addHoliday(US, WEEKEND_ONLY_HOLIDAYS);

    final SimpleRegion usRegion = new SimpleRegion();
    usRegion.addExternalId(US);
    REGION_SOURCE.addRegion(usRegion);

    SECURITY_SOURCE.addSecurity(USDLIBOR3M);
    SECURITY_SOURCE.addSecurity(USDLIBOR6M);
    SECURITY_SOURCE.addSecurity(USD_FEDFUND_INDEX);

    MARKET_VALUES.setDataPoint(MARKET_DATA_ID, RATE);
  }

  /**
   * Sets up the service context.
   */
  @BeforeSuite
  public static void setUp() {
    final VersionCorrectionProvider versionCorrectionProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };
    final ServiceContext serviceContext = ServiceContext.of(VersionCorrectionProvider.class, versionCorrectionProvider)
        .with(ConventionSource.class, CONVENTION_SOURCE)
        .with(SecuritySource.class, SECURITY_SOURCE);
    ThreadLocalServiceContext.init(serviceContext);
  }

 /**
  * Tests the behaviour when neither a convention nor security for a cash node cannot be found.
  */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionForCash() {
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, ExternalId.of(SCHEME, "Test"), MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        MARKET_VALUES, MARKET_DATA_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when the cash node references a convention that is not a deposit or ibor index convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongConventionTypeForCash() {
    final ExternalId swapConventionId = ExternalId.of(SCHEME, "Swap");
    final SwapConvention convention =
        new SwapConvention("Swap", swapConventionId.toBundle(), ExternalId.of(SCHEME, "Pay Leg"), ExternalId.of(SCHEME, "Receive Leg"));
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(convention);
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, swapConventionId, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        MARKET_VALUES, MARKET_DATA_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when the cash node references a security that is neither an index nor overnight security.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityTypeForCash() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final ExternalId equityId = ExternalId.of(SCHEME, "equity");
    final EquitySecurity equitySecurity = new EquitySecurity("", "", "", Currency.USD);
    equitySecurity.addExternalId(equityId);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(equitySecurity);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, equityId, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(securitySource, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    node.accept(converter);
  }

  /**
   * Tests that cash definition is created with the correct dates and conventions from a node
   * referencing a overnight index security.
   */
  @Test
  public void testOvernightIndexFromSecurity() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, USD_FEDFUND_INDEX_ID, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, RATE, 1. / 360);
    assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates and conventions from a node
   * referencing an overnight index convention.
   */
  @Test
  public void testOvernightIndexFromConvention() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, USD_OVERNIGHT_CONVENTION_ID, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, RATE, 1. / 360);
    assertEquals(definition, expected);
  }

  /**
   * Tests that an ibor deposit definition is created with the correct dates and conventions from a node
   * referencing a 3m ibor security.
   */
  @Test
  public void test3mLiborFromSecurity() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR3M_ID, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR_ACT_360_CONVENTION_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final DepositIborDefinition expected =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, RATE, 89d / 360d, index);
    assertEquals(definition, expected);
  }

  /**
   * Tests that an ibor deposit definition is created with the correct dates and conventions from a node
   * referencing an ibor index convention. The tenor is calculated from the start and maturity tenors.
   */
  @Test
  public void test3mLiborFromConvention() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR_ACT_360_CONVENTION_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final DepositIborDefinition expected =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, RATE, 89d / 360d, index);
    assertEquals(definition, expected);
  }

  /**
   * Tests that an ibor deposit definition is created with the correct dates and conventions from a node
   * referencing a 6m ibor security.
   */
  @Test
  public void test6mLiborFromSecurity() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
       new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.SIX_MONTHS, USDLIBOR6M_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR_ACT_360_CONVENTION_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    final DepositIborDefinition expected =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 8, 6), 1, RATE, 181d / 360d, index);
    assertEquals(definition, expected);
  }

  /**
   * Tests that an ibor deposit definition is created with the correct dates and conventions from a node
   * referencing an ibor index convention. The tenor is calculated from the start and maturity tenors.
   */
  @Test
  public void test6mLiborFromConvention() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
       new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.SIX_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR_ACT_360_CONVENTION_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    final DepositIborDefinition expected =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 8, 6), 1, RATE, 181d / 360d, index);
    assertEquals(definition, expected);
  }

  /**
   * Tests that an ibor deposit definition is created with the correct dates and conventions from a node
   * referencing a 6m ibor security. In this case, the rate is to be compounded.
   */
  @Test
  public void test3mCompoundedLiborFromSecurity() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR6M_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR_ACT_360_CONVENTION_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    final DepositIborDefinition expected =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, RATE, 89d / 360d, index);
    assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates from a one-day deposit node.
   */
  @Test
  public void testOneDayDeposit() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_DAY, DEPOSIT_1D_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, RATE, 1. / 360);
    assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates from an overnight deposit node.
   */
  @Test
  public void testOvernightDeposit() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, DEPOSIT_1D_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, RATE, 1. / 360);
    assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates from an overnight deposit node starting
   * in one day.
   */
  @Test
  public void testOneDayForwardOvernightDeposit() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CurveNode node = new CashNode(Tenor.ONE_DAY, Tenor.ON, DEPOSIT_1D_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 5, 3), 1, RATE, 1. / 360);
    assertEquals(definition, expected);
  }

 /**
  * Tests that a cash definition is created with the correct dates from a tom/next deposit node.
  */
  @Test
  public void testTomNextDeposit() {
   final ZonedDateTime now = DateUtils.getUTCDate(2013, 12, 20);
   final CurveNodeVisitor<InstrumentDefinition<?>> converter =
       new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
   final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.TN, DEPOSIT_1D_ID, MAPPER);
   final InstrumentDefinition<?> definition = node.accept(converter);
   final CashDefinition expected =
       new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 12, 20), DateUtils.getUTCDate(2013, 12, 24), 1, RATE, 4. / 360);
   assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates from a one month deposit node.
   */
  @Test
  public void testOneMonthDeposit() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    final CurveNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, MAPPER);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 3, 6), 1, RATE, 28. / 360);
    assertEquals(definition, expected);
  }

  /**
   * Tests that a cash definition is created with the correct dates from a one month deposit node that
   * has a maturity date on a weekend.
   */
  @Test
  public void testOneMonthDepositWeekendMaturity() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 2);
    final CashNodeConverter converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 6), DateUtils.getUTCDate(2013, 6, 6), 1, RATE, 31. / 360);
    assertEquals(definition, expected);
  }

 /**
  * Tests that a cash definition is created with the correct dates from a three month deposit that
  * starts one month forward.
  */
  @Test
  public void testOneMonthForwardThreeMonthDeposit() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 7);
    final CashNodeConverter converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, MARKET_VALUES, MARKET_DATA_ID, now);
    final CashNode node = new CashNode(Tenor.ONE_MONTH, Tenor.THREE_MONTHS, DEPOSIT_1M_ID, MAPPER);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final CashDefinition expected = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 6, 10), DateUtils.getUTCDate(2013, 9, 10), 1, RATE, 92. / 360);
    assertEquals(definition, expected);
  }
}
