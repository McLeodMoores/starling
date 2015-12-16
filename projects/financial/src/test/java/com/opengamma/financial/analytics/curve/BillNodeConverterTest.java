/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendarAdapter;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster;
import com.opengamma.master.legalentity.impl.MasterLegalEntitySource;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link BillNodeConverter}.
 */
@Test(groups = TestGroup.UNIT)
public class BillNodeConverterTest {
  /** An empty holiday source */
  private static final HolidaySource EMPTY_HOLIDAY_SOURCE = new MasterHolidaySource(new InMemoryHolidayMaster());
  /** An empty region source */
  private static final RegionSource EMPTY_REGION_SOURCE = new MasterRegionSource(new InMemoryRegionMaster());
  /** An empty security source */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new MasterSecuritySource(new InMemorySecurityMaster());
  /** An empty legal entity source */
  private static final LegalEntitySource EMPTY_LEGAL_ENTITY_SOURCE = new MasterLegalEntitySource(new InMemoryLegalEntityMaster());
  /** An empty market data bundle */
  private static final SnapshotDataBundle EMPTY_DATA_BUNDLE = new SnapshotDataBundle();
  /** An id */
  private static final ExternalId DATA_ID = ExternalSchemes.syntheticSecurityId("Test");
  /** The valuation time */
  private static final ZonedDateTime NOW = ZonedDateTime.now();
  /** The curve node id mapper name */
  private static final String MAPPER_NAME = "CNIM";

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource1() {
    new BillNodeConverter(null, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource2() {
    new BillNodeConverter(null, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the region source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionSource1() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, null, EMPTY_SECURITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the region source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionSource2() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, null, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecuritySource1() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, null, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecuritySource2() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, null, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the legal entity source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLegalEntitySource() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, null, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the market data bundle is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMarketDataBundle1() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, null, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the market data bundle is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMarketDataBundle2() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE, null, DATA_ID, NOW);
  }

  /**
   * Tests the behaviour when the data id is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataId1() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_DATA_BUNDLE, null, NOW);
  }

  /**
   * Tests the behaviour when the data id is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataId2() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, null, NOW);
  }

  /**
   * Tests the behaviour when the valuation time is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationTime1() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, null);
  }

  /**
   * Tests the behaviour when the valuation time is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationTime2() {
    new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE, EMPTY_DATA_BUNDLE, DATA_ID, null);
  }

  /**
   * Tests the behaviour when the data is not available from the data bundle.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testMissingMarketData() {
    final BillNodeConverter converter = new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE,
        EMPTY_DATA_BUNDLE, DATA_ID, NOW);
    final BillNode node = new BillNode(Tenor.ONE_YEAR, MAPPER_NAME);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when the bill security is not available from the source.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testMissingSecurity() {
    final SnapshotDataBundle data = new SnapshotDataBundle();
    data.setDataPoint(DATA_ID.toBundle(), 0.01);
    final BillNodeConverter converter = new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, EMPTY_SECURITY_SOURCE, EMPTY_LEGAL_ENTITY_SOURCE,
        data, DATA_ID, NOW);
    final BillNode node = new BillNode(Tenor.ONE_YEAR, MAPPER_NAME);
    node.accept(converter);
  }

  /**
   * Tests the behaviour when the id points to a security that is not a bill.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityType() {
    final SnapshotDataBundle data = new SnapshotDataBundle();
    data.setDataPoint(DATA_ID.toBundle(), 0.01);
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    final EquitySecurity equity = new EquitySecurity("", "", "", Currency.USD);
    equity.addExternalId(DATA_ID);
    securityMaster.add(new SecurityDocument(equity));
    final BillNodeConverter converter = new BillNodeConverter(EMPTY_HOLIDAY_SOURCE, EMPTY_REGION_SOURCE, new MasterSecuritySource(securityMaster), EMPTY_LEGAL_ENTITY_SOURCE,
        data, DATA_ID, NOW);
    final BillNode node = new BillNode(Tenor.ONE_YEAR, MAPPER_NAME);
    node.accept(converter);
  }

  /**
   * Tests that the correct instrument definition is returned.
   */
  @Test
  public void testDefinition() {
    final ExternalId regionId = ExternalSchemes.financialRegionId("US");
    final SimpleRegion region = new SimpleRegion();
    region.addExternalId(regionId);
    final InMemoryRegionMaster regionMaster = new InMemoryRegionMaster();
    regionMaster.add(new RegionDocument(region));
    final RegionSource regionSource = new MasterRegionSource(regionMaster);
    final SimpleHolidayWithWeekend holiday = new SimpleHolidayWithWeekend(Collections.<LocalDate>emptySet(), WeekendType.FRIDAY_SATURDAY);
    holiday.setType(HolidayType.BANK);
    holiday.setRegionExternalId(regionId);
    final InMemoryHolidayMaster holidayMaster = new InMemoryHolidayMaster();
    holidayMaster.add(new HolidayDocument(holiday));
    final HolidaySource holidaySource = new MasterHolidaySource(holidayMaster);
    final WorkingDayCalendar calendar =
        new WorkingDayCalendarAdapter(CalendarUtils.getCalendar(regionSource, holidaySource, regionId), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    final ExternalId legalEntityId = ExternalId.of("Len", "Test");
    final ZonedDateTime maturityDate = DateUtils.getUTCDate(2016, 1, 1);
    final int daysToSettle = 2;
    final YieldConvention yieldConvention = SimpleYieldConvention.DISCOUNT;
    final DayCount dayCount = DayCounts.ACT_360;
    final String isin = "US00000000";
    final BillSecurity billSecurity = new BillSecurity(Currency.USD, new Expiry(maturityDate), DateUtils.getUTCDate(2015, 9, 1), 1000, daysToSettle,
        regionId, yieldConvention, dayCount, legalEntityId);
    billSecurity.addExternalId(DATA_ID);
    billSecurity.addExternalId(ExternalSchemes.isinSecurityId(isin));
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    securityMaster.add(new SecurityDocument(billSecurity));
    final SnapshotDataBundle data = new SnapshotDataBundle();
    final double yield = 0.01;
    data.setDataPoint(DATA_ID.toBundle(), yield);
    final BillNode billNode = new BillNode(Tenor.THREE_MONTHS, MAPPER_NAME);
    // no legal entity source supplied, so the legal entity will contain only region information
    LegalEntity expectedLegalEntity = new LegalEntity(null, "", null, null, Region.of(regionId.getValue(), Country.US, Currency.USD));
    BillSecurityDefinition expectedBillSecurity = new BillSecurityDefinition(Currency.USD, maturityDate, 1, daysToSettle, calendar, yieldConvention, dayCount,
        expectedLegalEntity);
    BillTransactionDefinition expectedBillDefinition = BillTransactionDefinition.fromYield(expectedBillSecurity, 1, NOW, yield, calendar);
    BillNodeConverter converter = new BillNodeConverter(holidaySource, regionSource, new MasterSecuritySource(securityMaster), data, DATA_ID, NOW);
    BillTransactionDefinition billDefinition = (BillTransactionDefinition) billNode.accept(converter);
    assertEquals(billDefinition, expectedBillDefinition);
    // no legal entity available from source
    try {
      converter = new BillNodeConverter(holidaySource, regionSource, new MasterSecuritySource(securityMaster), EMPTY_LEGAL_ENTITY_SOURCE, data, DATA_ID, NOW);
      billDefinition = (BillTransactionDefinition) billNode.accept(converter);
      fail();
    } catch (final DataNotFoundException e) {
      //expected
    }
    // legal entity available from source
    final ManageableLegalEntity legalEntity = new ManageableLegalEntity("US Government", legalEntityId.toBundle());
    legalEntity.setRatings(Arrays.asList(new Rating("Moodys", CreditRating.A, SeniorityLevel.SNRFOR), new Rating("S&P", CreditRating.AA, SeniorityLevel.SNRFOR)));
    final InMemoryLegalEntityMaster legalEntityMaster = new InMemoryLegalEntityMaster();
    legalEntityMaster.add(new LegalEntityDocument(legalEntity));
    final Set<com.opengamma.analytics.financial.legalentity.CreditRating> creditRatings = Sets.newHashSet(
        com.opengamma.analytics.financial.legalentity.CreditRating.of("Moodys", CreditRating.A.toString(), true),
        com.opengamma.analytics.financial.legalentity.CreditRating.of("S&P", CreditRating.AA.toString(), true));
    expectedLegalEntity = new LegalEntity(isin, legalEntity.getName(), creditRatings, null, Region.of(regionId.getValue(), Country.US, Currency.USD));
    expectedBillSecurity = new BillSecurityDefinition(Currency.USD, maturityDate, 1, daysToSettle, calendar, yieldConvention, dayCount,
        expectedLegalEntity);
    expectedBillDefinition = BillTransactionDefinition.fromYield(expectedBillSecurity, 1, NOW, yield, calendar);
    converter = new BillNodeConverter(holidaySource, regionSource, new MasterSecuritySource(securityMaster), new MasterLegalEntitySource(legalEntityMaster), data, DATA_ID, NOW);
    billDefinition = (BillTransactionDefinition) billNode.accept(converter);
    assertEquals(billDefinition, expectedBillDefinition);
  }

  /**
   * Tests the conversion of a legal entity from the source to the one used in the analytics library.
   */
  @Test
  public void testCreateAnalyticsLegalEntity() {
    final ExternalId regionId = ExternalSchemes.financialRegionId("US");
    final SimpleRegion region = new SimpleRegion();
    region.addExternalId(regionId);
    final InMemoryRegionMaster regionMaster = new InMemoryRegionMaster();
    regionMaster.add(new RegionDocument(region));
    final RegionSource regionSource = new MasterRegionSource(regionMaster);
    final SimpleHolidayWithWeekend holiday = new SimpleHolidayWithWeekend(Collections.<LocalDate>emptySet(), WeekendType.FRIDAY_SATURDAY);
    holiday.setType(HolidayType.BANK);
    holiday.setRegionExternalId(regionId);
    final InMemoryHolidayMaster holidayMaster = new InMemoryHolidayMaster();
    holidayMaster.add(new HolidayDocument(holiday));
    final HolidaySource holidaySource = new MasterHolidaySource(holidayMaster);
    final ExternalId legalEntityId = ExternalId.of("Len", "Test");
    final BillSecurity billSecurity = new BillSecurity(Currency.USD, new Expiry(DateUtils.getUTCDate(2016, 1, 1)), DateUtils.getUTCDate(2015, 9, 1), 1000, 2,
        regionId, SimpleYieldConvention.DISCOUNT, DayCounts.ACT_360, legalEntityId);
    billSecurity.addExternalId(DATA_ID);
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    securityMaster.add(new SecurityDocument(billSecurity));
    final MasterSecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final SnapshotDataBundle data = new SnapshotDataBundle();
    data.setDataPoint(DATA_ID.toBundle(), 0.01);
    final BillNode billNode = new BillNode(Tenor.THREE_MONTHS, MAPPER_NAME);
    final InMemoryLegalEntityMaster legalEntityMaster = new InMemoryLegalEntityMaster();
    final BillNodeConverter converter = new BillNodeConverter(holidaySource, regionSource, securitySource, new MasterLegalEntitySource(legalEntityMaster), data, DATA_ID, NOW);
    // no ISIN
    ManageableLegalEntity legalEntity = new ManageableLegalEntity("US Government", legalEntityId.toBundle());
    legalEntity.setRatings(Arrays.asList(new Rating("Moodys", CreditRating.A, SeniorityLevel.SNRFOR), new Rating("S&P", CreditRating.AA, SeniorityLevel.SNRFOR)));
    LegalEntityDocument legalEntityDocument = legalEntityMaster.add(new LegalEntityDocument(legalEntity));
    final Set<com.opengamma.analytics.financial.legalentity.CreditRating> creditRatings = Sets.newHashSet(
        com.opengamma.analytics.financial.legalentity.CreditRating.of("Moodys", CreditRating.A.toString(), true),
        com.opengamma.analytics.financial.legalentity.CreditRating.of("S&P", CreditRating.AA.toString(), true));
    LegalEntity expectedLegalEntity = new LegalEntity(null, legalEntity.getName(), creditRatings, null, Region.of(regionId.getValue(), Country.US, Currency.USD));
    assertEquals(((BillTransactionDefinition) billNode.accept(converter)).getUnderlying().getIssuerEntity(), expectedLegalEntity);
    legalEntityMaster.remove(legalEntityDocument);
    // no credit ratings
    legalEntity = new ManageableLegalEntity("US Government", legalEntityId.toBundle());
    legalEntityDocument = legalEntityMaster.add(new LegalEntityDocument(legalEntity));
    expectedLegalEntity = new LegalEntity(null, legalEntity.getName(), null, null, Region.of(regionId.getValue(), Country.US, Currency.USD));
    assertEquals(((BillTransactionDefinition) billNode.accept(converter)).getUnderlying().getIssuerEntity(), expectedLegalEntity);
    legalEntityMaster.remove(legalEntityDocument);
  }
}

