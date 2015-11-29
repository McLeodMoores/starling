/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemoryHolidaySource;
import com.opengamma.engine.InMemoryRegionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class FraNodeConverterTest {
  /** The id scheme */
  private static final String SCHEME = "Test";
  /** US region identifier */
  private static final ExternalId US_REGION_ID = ExternalSchemes.countryRegionId(Country.US);
  /** GB region identifier */
  private static final ExternalId GB_REGION_ID = ExternalSchemes.countryRegionId(Country.GB);
  /** US holidays identifier */
  private static final ExternalId US_CALENDAR_ID = ExternalSchemes.countryRegionId(Country.US);
  /** GB holidays identifier */
  private static final ExternalId GB_CALENDAR_ID = ExternalSchemes.countryRegionId(Country.GB);
  /** The underlying ibor index convention id */
  private static final ExternalId USD_LIBOR_INDEX_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR Index Convention");
  /** The underlying ibor index id */
  private static final ExternalId USD_3M_LIBOR_INDEX_ID = ExternalId.of(SCHEME, "USD 3M LIBOR Index");
  /** The underlying ibor index convention */
  private static final IborIndexConvention USD_LIBOR_INDEX_CONVENTION = new IborIndexConvention("USD LIBOR Index Convention", USD_LIBOR_INDEX_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "Europe/London", GB_REGION_ID, US_CALENDAR_ID, "");
  /** The underlying ibor index security */
  private static final IborIndex USD_3M_LIBOR_INDEX = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, USD_LIBOR_INDEX_CONVENTION_ID);
  static {
    USD_3M_LIBOR_INDEX.setExternalIdBundle(USD_3M_LIBOR_INDEX_ID.toBundle());
  }
  /** The security source */
  private static final InMemorySecuritySource SECURITY_SOURCE = new InMemorySecuritySource();
  /** The convention source */
  private static final InMemoryConventionSource CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The holiday source */
  private static final InMemoryHolidaySource HOLIDAY_SOURCE = new InMemoryHolidaySource();
  /** The region source */
  private static final InMemoryRegionSource REGION_SOURCE = new InMemoryRegionSource();
  /** The valuation time */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2015, 1, 5);

  static {
    CONVENTION_SOURCE.addConvention(new SwapFixedLegConvention("USD Fixed Swap Leg", ExternalIdBundle.of(SCHEME, "USD Swap Fixed Leg"), Tenor.THREE_MONTHS, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
        Currency.USD, US_CALENDAR_ID, 0, false, StubType.NONE, false, 0));
    CONVENTION_SOURCE.addConvention(USD_LIBOR_INDEX_CONVENTION);
    // Holidays
    HOLIDAY_SOURCE.addHoliday(US_CALENDAR_ID, new SimpleHolidayWithWeekend(Collections.<LocalDate>emptyList(), WeekendType.SATURDAY_SUNDAY));
    HOLIDAY_SOURCE.addHoliday(GB_CALENDAR_ID, new SimpleHolidayWithWeekend(Collections.<LocalDate>emptyList(), WeekendType.SATURDAY_SUNDAY));
    // Regions
    final SimpleRegion usRegion = new SimpleRegion();
    final SimpleRegion gbRegion = new SimpleRegion();
    usRegion.addExternalId(US_REGION_ID);
    gbRegion.addExternalId(GB_REGION_ID);
    REGION_SOURCE.addRegion(usRegion);
    REGION_SOURCE.addRegion(gbRegion);
    SECURITY_SOURCE.addSecurity(USD_3M_LIBOR_INDEX);
  }

  /**
   * Tests the behaviour when there is no security or convention available for the underlying index referenced in the FRA node.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionOrSecurityForFraUnderlying() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests the behaviour when the FRA node refers to a convention that is not an ibor index convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionTypeForFra() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of(SCHEME, "USD Swap Fixed Leg"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests the behaviour when the FRA node refers to a security that is not an ibor index security.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityTypeForFra() {
    final OvernightIndex overnightIndex = new OvernightIndex("FED FUNDS", "USD FED FUNDS Index", ExternalId.of(SCHEME, "USD Overnight"));
    overnightIndex.setExternalIdBundle(ExternalIdBundle.of(SCHEME, "FED FUNDS"));
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(overnightIndex);
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of(SCHEME, "FED FUNDS"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(securitySource, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests the behaviour when the ibor security does not refer to an ibor index convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionTypeForIborIndex() {
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final ExternalId conventionId = ExternalId.of(SCHEME, "USD OVERNIGHT");
    final OvernightIndexConvention indexConvention = new OvernightIndexConvention("USD OVERNIGHT Index", conventionId.toBundle(), DayCounts.ACT_360, 0, Currency.USD, US_CALENDAR_ID);
    indexConvention.addExternalId(conventionId);
    conventionSource.addConvention(indexConvention);
    final IborIndex index = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, conventionId);
    index.addExternalId(USD_3M_LIBOR_INDEX_ID);
    securitySource.addSecurity(index);
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USD_3M_LIBOR_INDEX_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(securitySource, conventionSource, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests the behaviour when the fixing calendar cannot be found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoFixingCalendar() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USD_LIBOR_INDEX_CONVENTION_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, new InMemoryHolidaySource(), REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests the behaviour when the region calendar cannot be found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoRegionCalendar() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USD_LIBOR_INDEX_CONVENTION_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, new InMemoryHolidaySource(), REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  /**
   * Tests that the dates are calculated correctly. The valuation date is 2015-01-05, so the calculations should be: spot date = 2015-01-07,
   * fixing start = 2015-07-07, fixing end = 2015-10-07.
   */
  @Test
  public void testNoDateAdjustment() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime expectedFixingStart = DateUtils.getUTCDate(2015, 7, 7);
    final ZonedDateTime expectedFixingEnd = DateUtils.getUTCDate(2015, 10, 7);
    final Calendar fixingCalendar = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final com.opengamma.analytics.financial.instrument.index.IborIndex expectedIndex =
        new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, USD_LIBOR_INDEX_CONVENTION.getName());
    final ForwardRateAgreementDefinition expectedDefinition = ForwardRateAgreementDefinition.from(expectedFixingStart, expectedFixingEnd, 1, expectedIndex, rate, fixingCalendar);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USD_3M_LIBOR_INDEX_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    assertEquals(fraNode.accept(converter), expectedDefinition);
  }

  /**
   * Tests that the dates are calculated correctly. The valuation date is 2015-01-01, so the calculations should be: spot date = 2015-01-05,
   * fixing start = 2015-07-07, fixing end = 2015-10-07.
   */
  @Test
  public void testDateAdjustments() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 1, 5);
    final ZonedDateTime expectedFixingStart = DateUtils.getUTCDate(2015, 7, 7);
    final ZonedDateTime expectedFixingEnd = DateUtils.getUTCDate(2015, 10, 7);
    final Calendar fixingCalendar = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final com.opengamma.analytics.financial.instrument.index.IborIndex expectedIndex =
        new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, USD_LIBOR_INDEX_CONVENTION.getName());
    final ForwardRateAgreementDefinition expectedDefinition = ForwardRateAgreementDefinition.from(expectedFixingStart, expectedFixingEnd, 1, expectedIndex, rate, fixingCalendar);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USD_3M_LIBOR_INDEX_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, valuationDate);
    assertEquals(fraNode.accept(converter), expectedDefinition);
  }

  /**
   * Tests that the tenor from the index security is used preferentially when constructing the underlying index if is available from the source.
   * The difference between the tenors in the FRA node should only be used if the convention is referenced in the node.
   */
  @Test
  public void testTenorCalculation() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime expectedFixingStart = DateUtils.getUTCDate(2015, 7, 7);
    final ZonedDateTime expectedFixingEnd = DateUtils.getUTCDate(2016, 1, 7);
    final Calendar fixingCalendar = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    // underlying index has a three month tenor
    com.opengamma.analytics.financial.instrument.index.IborIndex expectedIndex =
        new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, USD_LIBOR_INDEX_CONVENTION.getName());
    FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.ONE_YEAR, USD_3M_LIBOR_INDEX_ID, "Mapper");
    ForwardRateAgreementDefinition expectedDefinition = ForwardRateAgreementDefinition.from(expectedFixingStart, expectedFixingEnd, 1, expectedIndex, rate, fixingCalendar);
    assertEquals(fraNode.accept(converter), expectedDefinition);
    // convention is used directly, so the FRA tenors are used for the underlying index tenor
    expectedIndex = new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD, Period.ofMonths(6), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, USD_LIBOR_INDEX_CONVENTION.getName());
    fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.ONE_YEAR, USD_LIBOR_INDEX_CONVENTION_ID, "Mapper");
    expectedDefinition = ForwardRateAgreementDefinition.from(expectedFixingStart, expectedFixingEnd, 1, expectedIndex, rate, fixingCalendar);
    assertEquals(fraNode.accept(converter), expectedDefinition);
  }
}
