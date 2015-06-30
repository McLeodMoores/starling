/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CashNodeConverter}.
 */
public class CashNodeConverterTest {
  /** */
  private static final SimpleHoliday WEEKEND_ONLY_HOLIDAYS = new SimpleHolidayWithWeekend(Collections.<LocalDate>emptySet(), WeekendType.SATURDAY_SUNDAY);
  /** */
  public static final String SCHEME = "Test";
  /** */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** */
  private static final ExternalId GB = ExternalSchemes.financialRegionId("GB");
  /** */
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  /** */
  private static final ExternalId DEPOSIT_1D_ID = ExternalId.of(SCHEME, "USD 1d Deposit");
  /** */
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  /** */
  private static final String USDLIBOR_ACT_360_CONVENTION_NAME = "USD Libor ACT/360";
  /** */
  private static final ExternalId USDLIBOR_ACT_360_CONVENTION_ID = ExternalId.of(SCHEME, USDLIBOR_ACT_360_CONVENTION_NAME);
  /** */
  private static final String USDLIBOR_30_360_CONVENTION_NAME = "USD Libor 30/360";
  /** */
  private static final ExternalId USDLIBOR_30_360_ID = ExternalId.of(SCHEME, USDLIBOR_30_360_CONVENTION_NAME);
  /** */
  private static final ExternalId USD_OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight");
  // LIBOR Index
  /** 1M LIBOR index name */
  private static final String USDLIBOR1M_NAME = "USDLIBOR1M";
  /** 1M LIBOR index ticker */
  private static final ExternalId USDLIBOR1M_ID = ExternalSchemes.bloombergTickerSecurityId("US0001M Index");
  /** 1M LIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR1M =
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR1M_NAME, "ICE LIBOR 1M - USD", Tenor.ONE_MONTH, USDLIBOR_ACT_360_CONVENTION_ID);
  static {
    USDLIBOR1M.addExternalId(USDLIBOR1M_ID);
  }
  /** 3M LIBOR index name */
  private static final String USDLIBOR3M_NAME = "USDLIBOR3M";
  /** 3M LIBOR index ticker */
  private static final ExternalId USDLIBOR3M_ID = ExternalSchemes.bloombergTickerSecurityId("US0003M Index");
  /** 3M LIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR3M =
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR3M_NAME, "ICE LIBOR 3M - USD", Tenor.THREE_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  static {
    USDLIBOR3M.addExternalId(USDLIBOR3M_ID);
  }
  /** 6M LIBOR index name */
  private static final String USDLIBOR6M_NAME = "USDLIBOR6M";
  /** 6M LIBOR index ticker */
  private static final ExternalId USDLIBOR6M_ID = ExternalSchemes.bloombergTickerSecurityId("US0006M Index");
  /** 6M LIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR6M =
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR6M_NAME, "ICE LIBOR 6M - USD", Tenor.SIX_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  static {
    USDLIBOR6M.addExternalId(USDLIBOR6M_ID);
  }
  // Fed funds
  /** Fed fund index name */
  private static final String USD_FEDFUND_INDEX_NAME = "Fed Funds Effective Rate";
  /** Fed fund ticker */
  private static final ExternalId USD_FEDFUND_INDEX_ID = ExternalSchemes.bloombergTickerSecurityId("FEDL1 Index");
  /** Fed fund index security */
  private static final OvernightIndex USD_FEDFUND_INDEX = new OvernightIndex(USD_FEDFUND_INDEX_NAME, USD_OVERNIGHT_CONVENTION_ID);
  static {
    USD_FEDFUND_INDEX.addExternalId(USD_FEDFUND_INDEX_ID);
  }
  /** */
  private static final DepositConvention DEPOSIT_1D = new DepositConvention("USD 1d Deposit", ExternalIdBundle.of(DEPOSIT_1D_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 0, false, Currency.USD, US);
  /** */
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", ExternalIdBundle.of(DEPOSIT_1M_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  /** */
  private static final IborIndexConvention USDLIBOR_ACT_360 =
      new IborIndexConvention(USDLIBOR_ACT_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_ACT_360_CONVENTION_ID),
          DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  /** */
  private static final IborIndexConvention LIBOR_30_360 = new IborIndexConvention(USDLIBOR_30_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_30_360_ID),
      DayCounts.THIRTY_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");

  // EUR conventions
  /** */
  private static final String EURIBOR_CONVENTION_NAME = "EUR Euribor";
  /** */
  private static final ExternalId EURIBOR_CONVENTION_ID = ExternalId.of(SCHEME, EURIBOR_CONVENTION_NAME);
  /** */
  private static final IborIndexConvention EURIBOR_CONVENTION = new IborIndexConvention(EURIBOR_CONVENTION_NAME, ExternalIdBundle.of(EURIBOR_CONVENTION_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.EUR, LocalTime.of(11, 0), "EU", EU, EU, "Page");
  // EURIBOR Index
  /** 1M EURIBOR index name */
  private static final String EURIBOR1M_NAME = "EURIBOR1M";
  /** 1M EURIBOR index ticker */
  private static final ExternalId EURIBOR1M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR001M Index");
  /** 1M EURIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR1M =
      new com.opengamma.financial.security.index.IborIndex(EURIBOR1M_NAME, "EURIBOR 1M ACT/360", Tenor.ONE_MONTH, EURIBOR_CONVENTION_ID);
  static {
    EURIBOR1M.addExternalId(EURIBOR1M_ID);
  }
  /** 3M EURIBOR index name */
  private static final String EURIBOR3M_NAME = "EURIBOR3M";
  /** 3M EURIBOR index ticker */
  private static final ExternalId EURIBOR3M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR003M Index");
  /** 3M EURIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR3M =
      new com.opengamma.financial.security.index.IborIndex(EURIBOR3M_NAME, "EURIBOR 3M ACT/360", Tenor.THREE_MONTHS, EURIBOR_CONVENTION_ID);
  static {
    EURIBOR3M.addExternalId(EURIBOR3M_ID);
  }
  /** 6M EURIBOR index name */
  private static final String EURIBOR6M_NAME = "EURIBOR6M";
  /** 6M EURIBOR index ticker */
  private static final ExternalId EURIBOR6M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR006M Index");
  /** 6M EURIBOR index security */
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR6M =
      new com.opengamma.financial.security.index.IborIndex(EURIBOR6M_NAME, "EURIBOR 6M ACT/360", Tenor.SIX_MONTHS, EURIBOR_CONVENTION_ID);
  static {
    EURIBOR6M.addExternalId(EURIBOR6M_ID);
  }
  /** */
  private static final String EUR_OVERNIGHT_CONVENTION_NAME = "EUR Overnight";
  /** */
  private static final ExternalId EUR_OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, EUR_OVERNIGHT_CONVENTION_NAME);
  /** */
  private static final OvernightIndexConvention EUR_OVERNIGHT_CONVENTION = new OvernightIndexConvention(EUR_OVERNIGHT_CONVENTION_NAME,
      ExternalIdBundle.of(EUR_OVERNIGHT_CONVENTION_ID), DayCounts.ACT_360, 0, Currency.EUR, EU);
  /** EONIA index name */
  private static final String EUR_EONIA_INDEX_NAME = "EUR EONIA";
  /** EONIA ticker */
  private static final ExternalId EUR_EONIA_INDEX_ID = ExternalSchemes.bloombergTickerSecurityId("EONIA Index");
  /** EONIA security */
  private static final OvernightIndex EUR_EONIA_INDEX = new OvernightIndex(EUR_EONIA_INDEX_NAME, EUR_OVERNIGHT_CONVENTION_ID);
  static {
    EUR_EONIA_INDEX.addExternalId(EUR_EONIA_INDEX_ID);
  }

  /** */
  private static final InMemorySecuritySource SECURITY_SOURCE = new InMemorySecuritySource();
  /** */
  private static final InMemoryConventionSource CONVENTION_SOURCE = new InMemoryConventionSource();
  /** */
  private static final InMemoryHolidaySource HOLIDAY_SOURCE = new InMemoryHolidaySource();
  /** */
  private static final InMemoryRegionSource REGION_SOURCE = new InMemoryRegionSource();
  /** */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 5, 1);

  static {
    CONVENTION_SOURCE.addConvention(DEPOSIT_1D);
    CONVENTION_SOURCE.addConvention(DEPOSIT_1M);
    CONVENTION_SOURCE.addConvention(USDLIBOR_ACT_360);
    CONVENTION_SOURCE.addConvention(LIBOR_30_360);
    // EUR
    CONVENTION_SOURCE.addConvention(EUR_OVERNIGHT_CONVENTION);
    CONVENTION_SOURCE.addConvention(EURIBOR_CONVENTION);
    // Holidays
    HOLIDAY_SOURCE.addHoliday(US, WEEKEND_ONLY_HOLIDAYS);
    HOLIDAY_SOURCE.addHoliday(EU, WEEKEND_ONLY_HOLIDAYS);
    HOLIDAY_SOURCE.addHoliday(GB, WEEKEND_ONLY_HOLIDAYS);
    // Regions
    final SimpleRegion usRegion = new SimpleRegion();
    final SimpleRegion euRegion = new SimpleRegion();
    final SimpleRegion gbRegion = new SimpleRegion();
    usRegion.addExternalId(US);
    euRegion.addExternalId(EU);
    gbRegion.addExternalId(GB);
    REGION_SOURCE.addRegion(usRegion);
    REGION_SOURCE.addRegion(euRegion);
    REGION_SOURCE.addRegion(gbRegion);
    // Securities
    SECURITY_SOURCE.addSecurity(USD_FEDFUND_INDEX);
    SECURITY_SOURCE.addSecurity(USDLIBOR1M);
    SECURITY_SOURCE.addSecurity(USDLIBOR3M);
    SECURITY_SOURCE.addSecurity(USDLIBOR6M);
    SECURITY_SOURCE.addSecurity(EUR_EONIA_INDEX);
    SECURITY_SOURCE.addSecurity(EURIBOR1M);
    SECURITY_SOURCE.addSecurity(EURIBOR3M);
    SECURITY_SOURCE.addSecurity(EURIBOR6M);

  }

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
        .with(ConventionSource.class, CONVENTION_SOURCE);
//        .with(SecuritySource.class, SECURITY_SOURCE);
    ThreadLocalServiceContext.init(serviceContext);
  }

  /**
   *
   */
  @Test
  public void testLibor() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    // 3M node on 3M index
    CurveNode iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR3M_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    final IborIndex ibor3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    DepositIborDefinition ibor = (DepositIborDefinition) definition;
    DepositIborDefinition expectedLibor =
        new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, rate, 89d / 360d, ibor3m);
    assertEquals("CurveNodeToDefinitionConverter: Libor fixing 3M", expectedLibor, ibor);
    // 6M Node on 6M index
    iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.SIX_MONTHS, USDLIBOR6M_ID, "Mapper");
    definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    ibor = (DepositIborDefinition) definition;
    final IborIndex ibor6m = ConverterUtils.indexIbor(USDLIBOR6M_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 8, 6), 1, rate, 181d / 360d, ibor6m);
    assertEquals(expectedLibor, ibor);
    // 3M node on 6M index
    iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR6M_ID, "Mapper");
    definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    ibor = (DepositIborDefinition) definition;
    expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, rate, 89d / 360d, ibor6m);
    assertEquals(expectedLibor, ibor);
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    cashNode.accept(converter);
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongConventionTypeForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final ExternalId swapConventionId = ExternalId.of(SCHEME, "Swap");
    final SwapConvention convention =
        new SwapConvention("Swap", swapConventionId.toBundle(), ExternalId.of(SCHEME, "Pay Leg"), ExternalId.of(SCHEME, "Receive Leg"));
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(convention);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, swapConventionId, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    cashNode.accept(converter);
  }

  /**
   *
   */
  @Test
  public void testOneDayDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    // P0D-P1D
    final CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_DAY, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue("CashNode: converter with P0D-P1D", definition instanceof CashDefinition);
    final CashDefinition cash = (CashDefinition) definition;
    final CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P0D-P1D", expectedCash, cash);
    // P0D-ON
    final CurveNode cashNodeON = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definitionON = cashNodeON.accept(converter);
    assertTrue("CashNode: converter with P0D-ON", definitionON instanceof CashDefinition);
    final CashDefinition cashON = (CashDefinition) definitionON;
    final CashDefinition expectedCashON =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P0D-ON", expectedCashON, cashON);
    // P1D-ON
    final CurveNode cashNode1DON = new CashNode(Tenor.ONE_DAY, Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition1DON = cashNode1DON.accept(converter);
    assertTrue("CashNode: converter with P1D-ON", definition1DON instanceof CashDefinition);
    final CashDefinition cash1DON = (CashDefinition) definition1DON;
    final CashDefinition expectedCash1DON =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 5, 3), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P1D-ON", expectedCash1DON, cash1DON);
    // ON-ON
    final CurveNode cashNodeONON = new CashNode(Tenor.ONE_DAY, Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definitionONON = cashNodeONON.accept(converter);
    assertTrue("CashNode: converter with P1D-ON", definitionONON instanceof CashDefinition);
    final CashDefinition cashONON = (CashDefinition) definitionONON;
    final CashDefinition expectedCashONON =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 5, 3), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P1D-ON", expectedCashONON, cashONON);
    // POD-TN(WE)
    final ZonedDateTime now2 = DateUtils.getUTCDate(2013, 12, 20);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter2 =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now2);
    final CurveNode cashNode0DTN = new CashNode(Tenor.of(Period.ZERO), Tenor.TN, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition0DTN = cashNode0DTN.accept(converter2);
    assertTrue("CashNode: converter with P0D-TN", definition0DTN instanceof CashDefinition);
    final CashDefinition cash0DTN = (CashDefinition) definition0DTN;
    final CashDefinition expectedCash0DTN =
        new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 12, 20), DateUtils.getUTCDate(2013, 12, 24), 1, rate, 4. / 360);
    assertEquals("CashNode: converter with P0D-TN", expectedCash0DTN, cash0DTN);
  }

  /**
   *
   */
  @Test
  public void testOneMonthDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    CashDefinition cash = (CashDefinition) definition;
    CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 3, 6), 1, rate, 28. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 2);
    converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    cash = (CashDefinition) definition;
    expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 6), DateUtils.getUTCDate(2013, 6, 6), 1, rate, 31. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 7);
    converter = new CashNodeConverter(SECURITY_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    cashNode = new CashNode(Tenor.ONE_MONTH, Tenor.THREE_MONTHS, DEPOSIT_1M_ID, "Mapper");
    definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    cash = (CashDefinition) definition;
    expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 6, 10), DateUtils.getUTCDate(2013, 9, 10), 1, rate, 92. / 360);
    assertEquals(expectedCash, cash);
  }
}
