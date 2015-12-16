/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link ConventionsPopulator}.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionsPopulatorTest {
  /** An overnight index convention with no Quandl id */
  private static final OvernightIndexConvention C1 = new OvernightIndexConvention("1", ExternalIdBundle.of("CONVENTION", "ABC1"),
      DayCounts.ACT_360, 2, Currency.USD, ExternalSchemes.countryRegionId(Country.US));
  /** An overnight index convention with a single Quandl id */
  private static final OvernightIndexConvention C2 = new OvernightIndexConvention("2",
      ExternalIdBundle.of(ExternalId.of("CONVENTION", "ABC2"), QuandlConstants.ofCode("ABC2")), DayCounts.ACT_360, 2,
      Currency.USD, ExternalSchemes.countryRegionId(Country.US));
  /** An overnight index convention with multiple Quandl ids */
  private static final OvernightIndexConvention C3 = new OvernightIndexConvention("3",
      ExternalIdBundle.of(ExternalId.of("CONVENTION", "ABC3"), QuandlConstants.ofCode("ABC3"), QuandlConstants.ofCode("DEF3")),
          DayCounts.ACT_360, 2, Currency.USD, ExternalSchemes.countryRegionId(Country.US));
  /** An ibor index convention with no Quandl id */
  private static final IborIndexConvention C4 = new IborIndexConvention("4", ExternalIdBundle.of("CONVENTION", "ABC4"),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.countryRegionId(Country.US),
      ExternalSchemes.countryRegionId(Country.US), "");
  /** An ibor index convention with a single Quandl id */
  private static final IborIndexConvention C5 = new IborIndexConvention("5",
      ExternalIdBundle.of(ExternalId.of("CONVENTION", "ABC5"), QuandlConstants.ofCode("ABC5")),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.countryRegionId(Country.US),
      ExternalSchemes.countryRegionId(Country.US), "");
  /** An ibor index convention with no Quandl id */
  private static final IborIndexConvention C6 = new IborIndexConvention("6",
      ExternalIdBundle.of(ExternalId.of("CONVENTION", "ABC6"), QuandlConstants.ofCode("ABC6"), QuandlConstants.ofCode("DEF6")),
      DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.countryRegionId(Country.US),
      ExternalSchemes.countryRegionId(Country.US), "");
  /** A vanilla ibor leg convention with an underlying convention with no Quandl id */
  private static final VanillaIborLegConvention C7 = new VanillaIborLegConvention("7", ExternalIdBundle.of("CONVENTION", "ABC7"),
      ExternalId.of("CONVENTION", "ABC4"), false, "NONE", Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);
  /** A vanilla ibor leg convention with an underlying convention with a single Quandl id */
  private static final VanillaIborLegConvention C8 = new VanillaIborLegConvention("8", ExternalIdBundle.of("CONVENTION", "ABC8"),
      ExternalId.of("CONVENTION", "ABC5"), false, "NONE", Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);
  /** A vanilla ibor leg convention with an underlying convention with multiple Quandl ids */
  private static final VanillaIborLegConvention C9 = new VanillaIborLegConvention("9", ExternalIdBundle.of("CONVENTION", "ABC9"),
      ExternalId.of("CONVENTION", "ABC6"), false, "NONE", Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);
  /** A STIR future convention with an underlying convention with no Quandl id */
  private static final QuandlStirFutureConvention C10 = new QuandlStirFutureConvention("10", ExternalIdBundle.of("CONVENTION", "ABC10"),
      Currency.USD, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "US", 1000000, ExternalId.of("CONVENTION", "ABC4"), 3, DayOfWeek.WEDNESDAY.name(),
      ExternalSchemes.countryRegionId(Country.US));
  /** A STIR future convention with an underlying convention with a single Quandl id */
  private static final QuandlStirFutureConvention C11 = new QuandlStirFutureConvention("11", ExternalIdBundle.of("CONVENTION", "ABC10"),
      Currency.USD, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "US", 1000000, ExternalId.of("CONVENTION", "ABC5"), 3, DayOfWeek.WEDNESDAY.name(),
      ExternalSchemes.countryRegionId(Country.US));
  /** A STIR future convention with an underlying convention with multiple Quandl ids */
  private static final QuandlStirFutureConvention C12 = new QuandlStirFutureConvention("12", ExternalIdBundle.of("CONVENTION", "ABC10"),
      Currency.USD, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "US", 1000000, ExternalId.of("CONVENTION", "ABC6"), 3, DayOfWeek.WEDNESDAY.name(),
      ExternalSchemes.countryRegionId(Country.US));
  /** An overnight index security */
  private static final OvernightIndex OVERNIGHT_INDEX = new OvernightIndex(C2.getName(), C2.getName(), QuandlConstants.ofCode("ABC2"),
      QuandlConstants.ofCode("ABC2").toBundle());
  /** An ibor index security */
  private static final IborIndex IBOR_INDEX = new IborIndex("3M 5", "3M 5", Tenor.THREE_MONTHS, QuandlConstants.ofCode("ABC5"),
      QuandlConstants.ofCode("ABC5").toBundle());

  /**
   * Tests the behaviour when the conventions are null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventions() {
    new ConventionsPopulator(null);
  }

  /**
   * Tests the behaviour when the convention master is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionMaster() {
    new ConventionsPopulator(Collections.<ManageableConvention>emptySet()).init(null, new InMemorySecurityMaster());
  }

  /**
   * Tests that an overnight convention without Quandl ids is stored in the master but no security is created.
   */
  @Test
  public void testOvernightIndexNoQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Collections.<ManageableConvention>singleton(C1));
    populator.init(conventionMaster, securityMaster);
    // test the convention is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(OvernightIndexConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Collections.singletonList(C1));
    // test no security is added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(OvernightIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Tests that an overnight convention with a single Quandl id is stored in the master and that the appropriate
   * security is created.
   */
  @Test
  public void testOvernightIndexSingleQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Collections.<ManageableConvention>singleton(C2));
    populator.init(conventionMaster, securityMaster);
    // test convention is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(OvernightIndexConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Collections.singletonList(C2));
    // test security is added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(OvernightIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>singletonList(OVERNIGHT_INDEX));
  }

  /**
   * Tests that an overnight convention with multiple Quandl ids is stored in the master but no security is created.
   */
  @Test
  public void testOvernightIndexMultplieQuandlCodes() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Collections.<ManageableConvention>singleton(C3));
    populator.init(conventionMaster, securityMaster);
    // test convention is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(OvernightIndexConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Collections.singletonList(C3));
    // test no security is added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(OvernightIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Tests that a vanilla ibor leg convention with an underlying convention without Quandl ids is stored in
   * the master but no security is created.
   */
  @Test
  public void testVanillaSwapLegIborIndexNoQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C4, C7));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(VanillaIborLegConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C4, C7));
    // test the security is not added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Tests that a vanilla ibor leg convention with an underlying convention with a single Quandl id is stored in the master and that the appropriate
   * security is created.
   */
  @Test
  public void testVanillaSwapLegIborIndexSingleQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C5, C8));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(VanillaIborLegConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C5, C8));
    // test the security is added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>singletonList(IBOR_INDEX));
  }

  /**
   * Tests that a vanilla ibor leg convention with an underlying convention with multiple Quandl ids is stored in the master but
   * no security is created.
   */
  @Test
  public void testVanillaSwapLegIborIndexMultipleQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C6, C9));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(VanillaIborLegConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C6, C9));
    // test the security is not added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Tests that a STIR future convention with an underlying convention without Quandl ids is stored in
   * the master but no security is created.
   */
  @Test
  public void testStirFutureIborIndexNoQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C4, C10));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(QuandlStirFutureConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C4, C10));
    // test the security is not added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Tests that a STIR future convention with an underlying convention with a single Quandl id is stored in the master and that the appropriate
   * security is created.
   */
  @Test
  public void testStirFutureIborIndexSingleQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C5, C11));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(QuandlStirFutureConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C5, C11));
    // test the security is added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>singletonList(IBOR_INDEX));
  }

  /**
   * Tests that a STIR future convention convention with an underlying convention with multiple Quandl ids is stored in the master but
   * no security is created.
   */
  @Test
  public void testStirFutureIborIndexMultipleQuandlCode() {
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final ConventionsPopulator populator = new ConventionsPopulator(Sets.<ManageableConvention>newHashSet(C6, C12));
    populator.init(conventionMaster, securityMaster);
    // test the ibor leg is added
    final ConventionSearchRequest conventionRequest = new ConventionSearchRequest();
    conventionRequest.setConventionType(QuandlStirFutureConvention.TYPE);
    final ConventionSearchResult conventionResult = conventionMaster.search(conventionRequest);
    final List<ManageableConvention> conventions = conventionResult.getConventions();
    assertConventionEquals(conventions, Arrays.asList(C6, C12));
    // test the security is not added
    final SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setSecurityType(IborIndex.INDEX_TYPE);
    final SecuritySearchResult securityResult = securityMaster.search(securityRequest);
    final List<ManageableSecurity> securities = securityResult.getSecurities();
    assertSecuritiesEquals(securities, Collections.<ManageableSecurity>emptyList());
  }

  /**
   * Method that tests that two lists of conventions are equal. The expected conventions have not been stored in the master and so
   * the unique ids will not be set.
   * @param actual  the actual conventions
   * @param expected  the expected conventions
   */
  private static void assertConventionEquals(final List<? extends ManageableConvention> actual, final List<? extends ManageableConvention> expected) {
    if (expected == null) {
      assertNull(actual);
      return;
    }
    if (expected.isEmpty()) {
      assertTrue(actual.isEmpty());
      return;
    }
    assertEquals(actual.size(), expected.size());
    for (int i = 0; i < expected.size(); i++) {
      final ManageableConvention actualConvention = actual.get(i);
      final ManageableConvention expectedConvention = expected.get(i);
      JodaBeanUtils.equalIgnoring(actualConvention, expectedConvention, ManageableConvention.meta().uniqueId());
    }
  }

  /**
   * Method that tests that two securities are equal. The expected securities have not been stored in the master and so
   * the unique ids will not be set.
   * @param actual  the actual securities
   * @param expected  the expected securities
   */
  private static void assertSecuritiesEquals(final List<? extends ManageableSecurity> actual, final List<? extends ManageableSecurity> expected) {
    if (expected == null) {
      assertNull(actual);
      return;
    }
    if (expected.isEmpty()) {
      assertTrue(actual.isEmpty());
      return;
    }
    assertEquals(actual.size(), expected.size());
    for (int i = 0; i < expected.size(); i++) {
      final ManageableSecurity actualSecurity = actual.get(i);
      final ManageableSecurity expectedSecurity = expected.get(i);
      JodaBeanUtils.equalIgnoring(actualSecurity, expectedSecurity, ManageableConvention.meta().uniqueId());
    }
  }
}
