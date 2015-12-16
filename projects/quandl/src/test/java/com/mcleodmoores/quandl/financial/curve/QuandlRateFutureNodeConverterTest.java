/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import static com.mcleodmoores.quandl.convention.ConventionTestInstances.IBOR_INDEX;
import static com.mcleodmoores.quandl.convention.ConventionTestInstances.OVERNIGHT_INDEX;
import static com.mcleodmoores.quandl.convention.ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE;
import static com.mcleodmoores.quandl.convention.ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.testutils.ListCalendar;
import com.opengamma.DataNotFoundException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemoryHolidaySource;
import com.opengamma.engine.InMemoryRegionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlRateFutureNodeConverter}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlRateFutureNodeConverterTest {
  /** Price of the first Eurodollar future */
  private static final Double EDH5_PRICE = 0.98;
  /** Id of the first Eurodollar future */
  private static final ExternalId EDH5_ID = QuandlConstants.ofCode("EDH5");
  /** Price of the second Eurodollar future */
  private static final Double EDM5_PRICE = 0.95;
  /** Id of the second Eurodollar future */
  private static final ExternalId EDM5_ID = QuandlConstants.ofCode("EDM5");
  /** Id of the Eurodollar future convention */
  private static final ExternalId ED_CONVENTION_ID = QUANDL_USD_3M_3M_STIR_FUTURE.getExternalIdBundle().iterator().next();
  /** Id of the ibor index convention */
  private static final ExternalId IBOR_INDEX_CONVENTION_ID = IBOR_INDEX.getExternalIdBundle().iterator().next();
  /** The ibor index security */
  private static final IborIndex IBOR_INDEX_SECURITY = new IborIndex("USD 3M LIBOR SECURITY", "USD 3M LIBOR", Tenor.THREE_MONTHS, IBOR_INDEX_CONVENTION_ID);
  /** The ibor index security id */
  private static final ExternalId IBOR_INDEX_SECURITY_ID = ExternalId.of(IBOR_INDEX_CONVENTION_ID.getScheme(), IBOR_INDEX_CONVENTION_ID.getValue());
  /** Price of the first Fed funds future */
  private static final Double FFH5_PRICE = 0.99;
  /** Id of the first Fed funds future */
  private static final ExternalId FFH5_ID = QuandlConstants.ofCode("FFH5");
  /** Price of the second Fed funds future */
  private static final Double FFM5_PRICE = 0.97;
  /** Id of the second Fed funds future */
  private static final ExternalId FFM5_ID = QuandlConstants.ofCode("FFM5");
  /** Id of the Fed funds future convention */
  private static final ExternalId FF_CONVENTION_ID = QUANDL_FED_FUNDS_FUTURE.getExternalIdBundle().iterator().next();
  /** Id of the overnight index convention */
  private static final ExternalId OVERNIGHT_INDEX_CONVENTION_ID = OVERNIGHT_INDEX.getExternalIdBundle().iterator().next();
  /** The overnight index security */
  private static final OvernightIndex OVERNIGHT_INDEX_SECURITY = new OvernightIndex("USD OVERNIGHT SECURITY", OVERNIGHT_INDEX_CONVENTION_ID);
  /** The overnight index security id */
  private static final ExternalId OVERNIGHT_INDEX_SECURITY_ID = ExternalId.of(OVERNIGHT_INDEX_CONVENTION_ID.getScheme(),
      OVERNIGHT_INDEX_CONVENTION_ID.getValue());
  /** The market data snapshot */
  private static final SnapshotDataBundle SNAPSHOT = new SnapshotDataBundle();
  /** The valuation time */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2015, 3, 1);
  /** The holiday dates */
  private static final Set<LocalDate> HOLIDAYS = Sets.newHashSet(LocalDate.of(2015, 6, 17), LocalDate.of(2015, 6, 30));
  /** The calendar */
  private static final Calendar US_CALENDAR = new ListCalendar("US Holidays", HOLIDAYS, new MondayToFridayCalendar("Weekend"));
  /** A US region holiday */
  private static final SimpleHoliday US_HOLIDAY = new SimpleHoliday(HOLIDAYS);
  /** The region */
  private static final SimpleRegion US_REGION = new SimpleRegion();

  static {
    SNAPSHOT.setDataPoint(EDH5_ID, EDH5_PRICE);
    SNAPSHOT.setDataPoint(EDM5_ID, EDM5_PRICE);
    SNAPSHOT.setDataPoint(FFH5_ID, FFH5_PRICE);
    SNAPSHOT.setDataPoint(FFM5_ID, FFM5_PRICE);
    US_HOLIDAY.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    US_HOLIDAY.setCurrency(Currency.USD);
    US_HOLIDAY.setType(HolidayType.BANK);
    IBOR_INDEX_SECURITY.setExternalIdBundle(IBOR_INDEX_SECURITY_ID.toBundle());
    OVERNIGHT_INDEX_SECURITY.setExternalIdBundle(OVERNIGHT_INDEX_SECURITY_ID.toBundle());
    US_REGION.addExternalId(ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when there is no data for the rate in the snapshot.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingMarketData() {
    final RateFutureNode node = new RateFutureNode(3, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final ConventionSource conventionSource = new InMemoryConventionSource();
    final HolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, QuandlConstants.ofCode("EDU5"), NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when there is no future convention in the convention source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingFutureConvention() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final ConventionSource conventionSource = new InMemoryConventionSource();
    final HolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when there is no underlying ibor index convention in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingIborConvention() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final HolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_USD_3M_3M_STIR_FUTURE);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    node.accept(converter);
  }


  /**
   * Tests the behaviour when there is no underlying overnight index convention in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingOvernightConvention() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FF_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final HolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_FED_FUNDS_FUTURE);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, FFH5_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when there is no appropriate region in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingRegionForStir() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_USD_3M_3M_STIR_FUTURE);
    conventionSource.addConvention(IBOR_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when there is no appropriate region in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingRegionForFedFunds() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ON, FF_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final RegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_FED_FUNDS_FUTURE);
    conventionSource.addConvention(OVERNIGHT_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    node.accept(converter);
  }

  /**
   * Tests that the expected definition is created when there is no ibor index security in the source but
   * the ibor index convention is present.
   */
  @Test
  public void testMissingIborSecurity() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_USD_3M_3M_STIR_FUTURE);
    conventionSource.addConvention(IBOR_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 3, 18, 16, 0), ZoneId.of("America/Chicago"));
    final com.opengamma.analytics.financial.instrument.index.IborIndex index = new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD,
        Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true, IBOR_INDEX.getName());
    final double paymentAccrualFactor = 0.25;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final InterestRateFutureSecurityDefinition expectedUnderlying =
        new InterestRateFutureSecurityDefinition(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final InterestRateFutureTransactionDefinition expected = new InterestRateFutureTransactionDefinition(expectedUnderlying, 1, NOW, EDH5_PRICE);
    assertEquals(definition, expected);
  }

  /**
   * Tests that the expected definition is created when there is no overnight index security in the source but
   * the overnight index convention is present.
   */
  @Test
  public void testMissingOvernightSecurity() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ON, FF_CONVENTION_ID, "Mapper");
    final SecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    conventionSource.addConvention(QUANDL_FED_FUNDS_FUTURE);
    conventionSource.addConvention(OVERNIGHT_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, FFH5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 3, 31, 16, 0), ZoneId.of("America/Chicago"));
    final IndexON index = new IndexON("USD OVERNIGHT INDEX", Currency.USD, DayCounts.ACT_360, 1);
    final double paymentAccrualFactor = 1 / 12.;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final FederalFundsFutureSecurityDefinition expectedUnderlying =
        FederalFundsFutureSecurityDefinition.from(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final FederalFundsFutureTransactionDefinition expected = new FederalFundsFutureTransactionDefinition(expectedUnderlying, 1, NOW, FFH5_PRICE);
    assertEquals(definition, expected);
  }

  /**
   * Tests that the expected definition is created when the ibor index security and convention are available.
   */
  @Test
  public void testEdh5() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    securitySource.addSecurity(IBOR_INDEX_SECURITY);
    conventionSource.addConvention(QUANDL_USD_3M_3M_STIR_FUTURE);
    conventionSource.addConvention(IBOR_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDH5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 3, 18, 16, 0), ZoneId.of("America/Chicago"));
    final com.opengamma.analytics.financial.instrument.index.IborIndex index = new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD,
        Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true, IBOR_INDEX_SECURITY.getName());
    final double paymentAccrualFactor = 0.25;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final InterestRateFutureSecurityDefinition expectedUnderlying =
        new InterestRateFutureSecurityDefinition(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final InterestRateFutureTransactionDefinition expected = new InterestRateFutureTransactionDefinition(expectedUnderlying, 1, NOW, EDH5_PRICE);
    assertEquals(definition, expected);
  }

  /**
   * Tests that the expected definition is created when the ibor index security and convention are available. In this case, the
   * future last trade date is a holiday.
   */
  @Test
  public void testEdm5() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ED_CONVENTION_ID, "Mapper");
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    securitySource.addSecurity(IBOR_INDEX_SECURITY);
    conventionSource.addConvention(QUANDL_USD_3M_3M_STIR_FUTURE);
    conventionSource.addConvention(IBOR_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, EDM5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 6, 18, 16, 0), ZoneId.of("America/Chicago"));
    final com.opengamma.analytics.financial.instrument.index.IborIndex index = new com.opengamma.analytics.financial.instrument.index.IborIndex(Currency.USD,
        Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true, IBOR_INDEX_SECURITY.getName());
    final double paymentAccrualFactor = 0.25;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final InterestRateFutureSecurityDefinition expectedUnderlying =
        new InterestRateFutureSecurityDefinition(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final InterestRateFutureTransactionDefinition expected = new InterestRateFutureTransactionDefinition(expectedUnderlying, 1, NOW, EDM5_PRICE);
    assertEquals(definition, expected);
  }

  /**
   * Tests that the expected definition is created when the overnight index security and convention are available.
   */
  @Test
  public void testFfh5() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ON, FF_CONVENTION_ID, "Mapper");
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    securitySource.addSecurity(OVERNIGHT_INDEX_SECURITY);
    conventionSource.addConvention(QUANDL_FED_FUNDS_FUTURE);
    conventionSource.addConvention(OVERNIGHT_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, FFH5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 3, 31, 16, 0), ZoneId.of("America/Chicago"));
    final IndexON index = new IndexON("USD OVERNIGHT INDEX", Currency.USD, DayCounts.ACT_360, 1);
    final double paymentAccrualFactor = 1 / 12.;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final FederalFundsFutureSecurityDefinition expectedUnderlying =
        FederalFundsFutureSecurityDefinition.from(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final FederalFundsFutureTransactionDefinition expected = new FederalFundsFutureTransactionDefinition(expectedUnderlying, 1, NOW, FFH5_PRICE);
    assertEquals(definition, expected);
  }

  /**
   * Tests that the expected definition is created when the overnight index security and convention are available. In this case, the
   * future last trade date is a holiday.
   */
  @Test
  public void testFfm5() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ON, FF_CONVENTION_ID, "Mapper");
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final InMemoryRegionSource regionSource = new InMemoryRegionSource();
    securitySource.addSecurity(OVERNIGHT_INDEX_SECURITY);
    conventionSource.addConvention(QUANDL_FED_FUNDS_FUTURE);
    conventionSource.addConvention(OVERNIGHT_INDEX);
    holidaySource.addHoliday(ExternalSchemes.countryRegionId(Country.US), US_HOLIDAY);
    regionSource.addRegion(US_REGION);
    final QuandlRateFutureNodeConverter converter = new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource,
        SNAPSHOT, FFM5_ID, NOW);
    final ZonedDateTime expiryDate = ZonedDateTime.of(LocalDateTime.of(2015, 6, 30, 16, 0), ZoneId.of("America/Chicago"));
    final IndexON index = new IndexON("USD OVERNIGHT INDEX", Currency.USD, DayCounts.ACT_360, 1);
    final double paymentAccrualFactor = 1 / 12.;
    final InstrumentDefinition<?> definition = node.accept(converter);
    final FederalFundsFutureSecurityDefinition expectedUnderlying =
        FederalFundsFutureSecurityDefinition.from(expiryDate, index, 1, paymentAccrualFactor, "", US_CALENDAR);
    final FederalFundsFutureTransactionDefinition expected = new FederalFundsFutureTransactionDefinition(expectedUnderlying, 1, NOW, FFM5_PRICE);
    assertEquals(definition, expected);
  }
}
