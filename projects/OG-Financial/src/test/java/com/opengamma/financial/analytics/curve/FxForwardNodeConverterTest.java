/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemoryHolidaySource;
import com.opengamma.engine.InMemoryRegionSource;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link FXForwardNodeConverter}. This class tests both the original OpenGamma implementation and
 * the version that follows the FX market conventions in detail: the converter is backwards compatible.
 * <p>
 * The original behaviour of the converter is as follows:
 * <ul>
 *  <li> No special consideration of US holidays
 *  <li> T+n settlement to calculate the spot date
 *  <li> Add the maturity tenor and use the business day convention and settlement calendar from the convention
 *  <li> Only the settlement region holidays are considered
 *  <li> The business day convention that is used to find the settlement date is taken from the forward convention,
 *  rather than using modified following for all cases
 *  <li> Weekends are hard-coded to Saturday and Sunday (this behaviour comes from the holiday source rather than
 *  the converter)
 *  <li> South American currencies that should take US holidays on intermediate days into consideration when calculating
 *  the settlement date do not.
 * </ul>
 * The newer implementation of the code uses the following rules to calculate the delivery date:
 * <ul>
 *  <li> The tenor of the node is added to the valuation time.
 *  <li> T+n settlement must include n good business days, where good business days are defined as dates that are neither
 *  holidays nor weekends in either of the currencies in the pair, unless one of those currencies is USD, in which case
 *  USD holidays are ignored, unless one of the currencies is a special Latin American currency, in which case USD holidays
 *  are considered.
 *  <li> The delivery date cannot be a holiday in either of the currencies or USD. The date is moved forwards until this
 *  is true.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class FxForwardNodeConverterTest {
  /** The test scheme */
  private static final String SCHEME = "Test";
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** A region source */
  private static final InMemoryRegionSource REGION_SOURCE = new InMemoryRegionSource();
  /** EU region identifier */
  private static final ExternalId EU_REGION_ID = ExternalSchemes.countryRegionId(Country.EU);
  /** GB region identifier */
  private static final ExternalId GB_REGION_ID = ExternalSchemes.countryRegionId(Country.GB);
  /** US region identifier */
  private static final ExternalId US_REGION_ID = ExternalSchemes.countryRegionId(Country.US);
  /** SA region identifier */
  private static final ExternalId SA_REGION_ID = ExternalSchemes.countryRegionId(Country.of("SA"));
  /** MX region identifier */
  private static final ExternalId MX_REGION_ID = ExternalSchemes.countryRegionId(Country.MX);
  /** EU region */
  private static final SimpleRegion EU_REGION = new SimpleRegion();
  /** GB region */
  private static final SimpleRegion GB_REGION = new SimpleRegion();
  /** US region */
  private static final SimpleRegion US_REGION = new SimpleRegion();
  /** SA region */
  private static final SimpleRegion SA_REGION = new SimpleRegion();
  /** MX region */
  private static final SimpleRegion MX_REGION = new SimpleRegion();
  /** A currency holiday calendar containing no dates */
  private static final SimpleHoliday EMPTY_HOLIDAYS = new SimpleHoliday();
  /** A convention source */
  private static final InMemoryConventionSource CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The legacy GBP/EUR spot convention id */
  private static final ExternalId LEGACY_GBPEUR_SPOT_ID = ExternalId.of(SCHEME, "Legacy GBP/EUR Spot");
  /** The legacy GBP/EUR forward convention id */
  private static final ExternalId LEGACY_GBPEUR_FORWARD_ID = ExternalId.of(SCHEME, "Legacy GBP/EUR Forward");
  /** The legacy GBP/EUR spot convention */
  private static final FXSpotConvention LEGACY_GBPEUR_SPOT = new FXSpotConvention("Legacy GPB/EUR Spot", LEGACY_GBPEUR_SPOT_ID.toBundle(), 2, EU_REGION_ID);
  /** The legacy GBP/EUR forward convention */
  private static final FXForwardAndSwapConvention LEGACY_GBPEUR_FORWARD = new FXForwardAndSwapConvention("Legacy GBP/EUR Forward",
      LEGACY_GBPEUR_FORWARD_ID.toBundle(), LEGACY_GBPEUR_SPOT_ID, BusinessDayConventions.FOLLOWING, false, EU_REGION_ID);
  /** The GBP/EUR spot convention id */
  private static final ExternalId GBPEUR_SPOT_ID = ExternalId.of(SCHEME, "GBP/EUR Spot");
  /** The GBP/EUR forward convention id */
  private static final ExternalId GBPEUR_FORWARD_ID = ExternalId.of(SCHEME, "GBP/EUR Forward");
  /** The GBP/EUR spot convention */
  private static final FXSpotConvention GBPEUR_SPOT = new FXSpotConvention("GPB/EUR Spot", GBPEUR_SPOT_ID.toBundle(), 2, false);
  /** The GBP/EUR forward convention */
  private static final FXForwardAndSwapConvention GBPEUR_FORWARD = new FXForwardAndSwapConvention("GBP/EUR Forward",
      GBPEUR_FORWARD_ID.toBundle(), GBPEUR_SPOT_ID, BusinessDayConventions.FOLLOWING, false, EU_REGION_ID);
  /** The legacy USD/SAR spot convention id */
  private static final ExternalId LEGACY_USDSAR_SPOT_ID = ExternalId.of(SCHEME, "Legacy USD/SAR Spot");
  /** The legacy USD/SAR forward convention id */
  private static final ExternalId LEGACY_USDSAR_FORWARD_ID = ExternalId.of(SCHEME, "Legacy USD/SAR Forward");
  /** The legacy USD/SAR spot convention */
  private static final FXSpotConvention LEGACY_USDSAR_SPOT = new FXSpotConvention("Legacy USD/SAR Spot", LEGACY_USDSAR_SPOT_ID.toBundle(), 2, SA_REGION_ID);
  /** The legacy USD/SAR forward convention */
  private static final FXForwardAndSwapConvention LEGACY_USDSAR_FORWARD = new FXForwardAndSwapConvention("Legacy USD/SAR Forward",
      LEGACY_USDSAR_FORWARD_ID.toBundle(), LEGACY_USDSAR_SPOT_ID, BusinessDayConventions.FOLLOWING, false, SA_REGION_ID);
  /** The legacy USD/MXN spot convention id */
  private static final ExternalId LEGACY_USDMXN_SPOT_ID = ExternalId.of(SCHEME, "Legacy USD/MXN Spot");
  /** The legacy USD/MXN forward convention id */
  private static final ExternalId LEGACY_USDMXN_FORWARD_ID = ExternalId.of(SCHEME, "Legacy USD/MXN Forward");
  /** The legacy USD/MXN spot convention */
  private static final FXSpotConvention LEGACY_USDMXN_SPOT = new FXSpotConvention("Legacy USD/MXN Spot", LEGACY_USDMXN_SPOT_ID.toBundle(), 2, MX_REGION_ID);
  /** The legacy USD/MXN forward convention */
  private static final FXForwardAndSwapConvention LEGACY_USDMXN_FORWARD = new FXForwardAndSwapConvention("Legacy USD/MXN Forward",
      LEGACY_USDMXN_FORWARD_ID.toBundle(), LEGACY_USDMXN_SPOT_ID, BusinessDayConventions.FOLLOWING, false, MX_REGION_ID);
  /** The USD/MXN spot convention id */
  private static final ExternalId USDMXN_SPOT_ID = ExternalId.of(SCHEME, "USD/MXN Spot");
  /** The USD/MXN forward convention id */
  private static final ExternalId USDMXN_FORWARD_ID = ExternalId.of(SCHEME, "USD/MXN Forward");
  /** The USD/MXN spot convention */
  private static final FXSpotConvention USDMXN_SPOT = new FXSpotConvention("USD/MXN Spot", USDMXN_SPOT_ID.toBundle(), 2, true);
  /** The USD/MXN forward convention */
  private static final FXForwardAndSwapConvention USDMXN_FORWARD = new FXForwardAndSwapConvention("USD/MXN Forward",
      USDMXN_FORWARD_ID.toBundle(), USDMXN_SPOT_ID, BusinessDayConventions.FOLLOWING, false, MX_REGION_ID);
  /** The market data snapshot */
  private static final SnapshotDataBundle DATA = new SnapshotDataBundle();
  /** GBP/EUR data id */
  private static final ExternalId GBPEUR_DATA_ID = ExternalId.of(SCHEME, "GBPEUR");
  /** USD/SAR data id */
  private static final ExternalId USDSAR_DATA_ID = ExternalId.of(SCHEME, "USDSAR");
  /** USD/MXN data id */
  private static final ExternalId USDMXN_DATA_ID = ExternalId.of(SCHEME, "USDMXN");

  static {
    EU_REGION.setName("EU");
    EU_REGION.setCurrency(Currency.EUR);
    EU_REGION.setCountry(Country.EU);
    EU_REGION.setExternalIdBundle(EU_REGION_ID.toBundle());
    GB_REGION.setName("GB");
    GB_REGION.setCurrency(Currency.GBP);
    GB_REGION.setCountry(Country.GB);
    GB_REGION.setExternalIdBundle(GB_REGION_ID.toBundle());
    US_REGION.setName("US");
    US_REGION.setCurrency(Currency.USD);
    US_REGION.setCountry(Country.US);
    US_REGION.setExternalIdBundle(US_REGION_ID.toBundle());
    SA_REGION.setName("SA");
    SA_REGION.setCurrency(Currency.of("SAR"));
    SA_REGION.setCountry(Country.of("SA"));
    SA_REGION.setExternalIdBundle(SA_REGION_ID.toBundle());
    MX_REGION.setName("MX");
    MX_REGION.setCurrency(Currency.of("MXN"));
    MX_REGION.setCountry(Country.of("MX"));
    MX_REGION.setExternalIdBundle(MX_REGION_ID.toBundle());
    REGION_SOURCE.addRegion(EU_REGION);
    REGION_SOURCE.addRegion(GB_REGION);
    REGION_SOURCE.addRegion(US_REGION);
    REGION_SOURCE.addRegion(SA_REGION);
    REGION_SOURCE.addRegion(MX_REGION);
    CONVENTION_SOURCE.addConvention(LEGACY_GBPEUR_SPOT);
    CONVENTION_SOURCE.addConvention(LEGACY_GBPEUR_FORWARD);
    CONVENTION_SOURCE.addConvention(GBPEUR_SPOT);
    CONVENTION_SOURCE.addConvention(GBPEUR_FORWARD);
    CONVENTION_SOURCE.addConvention(LEGACY_USDSAR_SPOT);
    CONVENTION_SOURCE.addConvention(LEGACY_USDSAR_FORWARD);
    CONVENTION_SOURCE.addConvention(LEGACY_USDMXN_SPOT);
    CONVENTION_SOURCE.addConvention(LEGACY_USDMXN_FORWARD);
    CONVENTION_SOURCE.addConvention(USDMXN_SPOT);
    CONVENTION_SOURCE.addConvention(USDMXN_FORWARD);
    DATA.setDataPoint(GBPEUR_DATA_ID, 1.2);
    DATA.setDataPoint(USDSAR_DATA_ID, 3.75);
    DATA.setDataPoint(USDMXN_DATA_ID, 15.5);
    EMPTY_HOLIDAYS.setType(HolidayType.CURRENCY);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. There are no holidays between the maturity and
   * settlement dates and so the payment date is expected to be 13/5/2015.
   */
  @Test
  public void testLegacyNoHolidays20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    holidaySource.addHoliday(EU_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getPaymentCurrency1().getCurrency(), Currency.EUR);
    assertEquals(definition.getPaymentCurrency2().getCurrency(), Currency.GBP);
    assertEquals(definition.getPaymentCurrency1().getReferenceAmount(), 1.0);
    assertEquals(definition.getPaymentCurrency2().getReferenceAmount(), -1.2);
    assertEquals(definition.getPaymentCurrency1().getPaymentDate(), expectedPaymentDate);
    assertEquals(definition.getPaymentCurrency2().getPaymentDate(), expectedPaymentDate);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. There are no holidays on the
   * settlement date and so the payment date is expected to be 13/5/2015.
   */
  @Test
  public void testNoHolidays20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    holidaySource.addHoliday(Currency.EUR, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 1/5/2015. The settlement date is a weekend
   * and so the payment date is expected to be 12/5/2015.
   */
  @Test
  public void testLegacyNoHolidays20150501() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 1);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 12);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    holidaySource.addHoliday(EU_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 1/5/2015. The settlement date is a weekend
   * and so the payment date is expected to be 12/5/2015.
   */
  @Test
  public void testNoHolidays20150501() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 1);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 12);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    holidaySource.addHoliday(Currency.EUR, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 1M pay EUR, receive GBP FX forward node on 31/1/2015. The business day convention is taken from the
   * forward convention (following in this case) and so the payment date is expected to be 4/3/2015.
   */
  @Test
  public void testLegacyBusinessDayConvention20150131() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 1, 31);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 3, 4);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    holidaySource.addHoliday(EU_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a EU holiday and so the
   * payment date is expected to be 14/5/2015.
   */
  @Test
  public void testLegacyEuHoliday20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 14);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday euHoliday = new SimpleHoliday();
    euHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    holidaySource.addHoliday(EU_REGION_ID, euHoliday);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a EU holiday and so the
   * payment date is expected to be 14/5/2015.
   */
  @Test
  public void testEuHoliday20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 14);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday euHoliday = new SimpleHoliday();
    euHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    euHoliday.setType(HolidayType.CURRENCY);
    holidaySource.addHoliday(Currency.EUR, euHoliday);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 1/5/2015. The settlement date falls on a EU holiday and so the
   * payment date is expected to be 13/5/2015.
   */
  @Test
  public void testLegacyEuHoliday20150501() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 1);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday euHoliday = new SimpleHoliday();
    euHoliday.addHolidayDate(LocalDate.of(2015, 5, 12));
    holidaySource.addHoliday(EU_REGION_ID, euHoliday);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 1/5/2015. The settlement date falls on a EU holiday and so the
   * payment date is expected to be 13/5/2015.
   */
  @Test
  public void testEuHoliday20150501() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 1);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday euHoliday = new SimpleHoliday();
    euHoliday.addHolidayDate(LocalDate.of(2015, 5, 12));
    euHoliday.setType(HolidayType.CURRENCY);
    // note that the currencies are used to request holidays
    holidaySource.addHoliday(Currency.EUR, euHoliday);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, EMPTY_HOLIDAYS);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a GB holiday, which will be
   * ignored because only the settlement currency holidays are considered, and so the payment date is expected to be 13/5/2015.
   */
  @Test
  public void testLegacyGbHoliday20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday gbHoliday = new SimpleHoliday();
    gbHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    holidaySource.addHoliday(EU_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(GB_REGION_ID, gbHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a GB holiday and so the
   * payment date is expected to be 14/5/2015.
   */
  @Test
  public void testGbHoliday20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 14);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday gbHoliday = new SimpleHoliday();
    gbHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    gbHoliday.setType(HolidayType.CURRENCY);
    holidaySource.addHoliday(US_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.EUR, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.GBP, gbHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a US holiday but this is ignored
   * and so the payment date is expected to be 13/5/2015.
   */
  @Test
  public void testLegacyUsHolidays20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 13);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    usHoliday.setType(HolidayType.BANK);
    holidaySource.addHoliday(EU_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(GB_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay EUR, receive GBP FX forward node on 4/5/2015. The settlement date falls on a US holiday
   * and so the payment date is expected to be 14/5/2015.
   */
  @Test
  public void testUsHolidays20150504() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 4);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 14);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    usHoliday.setType(HolidayType.BANK);
    holidaySource.addHoliday(Currency.EUR, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.EUR, Currency.GBP, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay USD, receive SAR FX forward node on 13/5/2015. The settlement date falls on a SA weekend but this is ignored
   * and so the payment date is expected to be 22/5/2015.
   */
  @Test
  public void testLegacySaWeekend() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 13);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 22);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 13));
    // old holiday sources used hard-coded weekend days; this addHoliday() method does the same
    holidaySource.addHoliday(SA_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_USDSAR_FORWARD_ID, Currency.USD, Currency.of("SAR"), CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, USDSAR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }

  /**
   * Tests conversion of a 7D pay USD, receive MXN FX forward node on 13/5/2015. The day before settlement date is a US holiday but this is ignored
   * and so the payment date is expected to be 22/5/2015.
   */
  @Test
  public void testLegacyMxWithIntermediateUsHoliday() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 13);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 22);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 21));
    holidaySource.addHoliday(MX_REGION_ID, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), LEGACY_USDMXN_FORWARD_ID, Currency.USD, Currency.MXN, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, USDMXN_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }


  /**
   * Tests conversion of a 7D pay GBP, receive EUR FX forward node on 13/5/2015. The day before settlement date is a US holiday but the convention indicates
   * that it should be ignored and so the payment date is expected to be 22/5/2015.
   */
  @Test
  public void testGbWithIntermediateUsHoliday() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 13);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 22);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 21));
    holidaySource.addHoliday(Currency.EUR, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.GBP, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), GBPEUR_FORWARD_ID, Currency.GBP, Currency.EUR, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, GBPEUR_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }


  /**
   * Tests conversion of a 7D pay USD, receive MXN FX forward node on 13/5/2015. The day before settlement date is a US holiday
   * and so the payment date is expected to be 25/5/2015.
   */
  @Test
  public void testMxWithIntermediateUsHoliday() {
    final ZonedDateTime valuationDate = DateUtils.getUTCDate(2015, 5, 13);
    final ZonedDateTime expectedPaymentDate = DateUtils.getUTCDate(2015, 5, 25);
    final InMemoryHolidaySource holidaySource = new InMemoryHolidaySource();
    final SimpleHoliday usHoliday = new SimpleHoliday();
    usHoliday.addHolidayDate(LocalDate.of(2015, 5, 21));
    holidaySource.addHoliday(Currency.MXN, EMPTY_HOLIDAYS);
    holidaySource.addHoliday(Currency.USD, usHoliday);
    holidaySource.addHoliday(US_REGION_ID, usHoliday);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ofDays(7), USDMXN_FORWARD_ID, Currency.USD, Currency.MXN, CNIM_NAME);
    final FXForwardNodeConverter converter = new FXForwardNodeConverter(CONVENTION_SOURCE, holidaySource, REGION_SOURCE, DATA, USDMXN_DATA_ID, valuationDate);
    final ForexDefinition definition = (ForexDefinition) node.accept(converter);
    assertEquals(definition.getExchangeDate(), expectedPaymentDate);
  }
}
