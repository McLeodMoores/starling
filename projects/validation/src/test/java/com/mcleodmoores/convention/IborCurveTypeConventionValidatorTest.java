/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.convention.impl.MasterConventionSource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
/**
 * Unit tests for {@link IborCurveTypeConventionValidator}.
 */
public class IborCurveTypeConventionValidatorTest {
  /** An empty convention source */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new MasterConventionSource(new InMemoryConventionMaster());
  /** The validator */
  private static final ConventionValidator<CurveGroupConfiguration, IborIndexConvention> VALIDATOR =
      IborCurveTypeConventionValidator.getInstance();

  /**
   * Tests the behaviour when the curve group configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveGroupConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_CONVENTION_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    VALIDATOR.validate(group, null, EMPTY_CONVENTION_SOURCE);
  }

  /**
   * Tests the behaviour when the convention source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    VALIDATOR.validate(group, VersionCorrection.LATEST, (ConventionSource) null);
  }

  /**
   * Tests that other curve type configurations are not validated.
   */
  @Test
  public void testOvernightCurveType() {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new OvernightCurveTypeConfiguration(ExternalId.of("CONVENTION", "Test")));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final ConventionValidationInfo<IborIndexConvention> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, EMPTY_CONVENTION_SOURCE);
    assertEquals(validationInfo.getMissingConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConventions(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConventionIds(), Collections.emptySet());
  }

  /**
   * Tests that conventions that are of the expected type and uniquely available from the source are identified.
   */
  @Test
  public void testValidatedConventions() {
    final ExternalId conventionId1 = ExternalId.of("CONVENTION", "Test1");
    final ExternalId conventionId2 = ExternalId.of("CONVENTION", "Test2");
    final List<? extends CurveTypeConfiguration> type = Arrays.asList(new IborCurveTypeConfiguration(conventionId1, Tenor.THREE_MONTHS),
        new IborCurveTypeConfiguration(conventionId2, Tenor.SIX_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final InMemoryConventionMaster conventionMaster = new InMemoryConventionMaster();
    final IborIndexConvention convention1 = (IborIndexConvention) conventionMaster.add(new ConventionDocument(
        new IborIndexConvention("Ibor1", conventionId1.toBundle(), DayCounts.ACT_365, BusinessDayConventions.FOLLOWING,
            2, false, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.financialRegionId("US"),
            ExternalSchemes.financialRegionId("US"), ""))).getConvention();
    final IborIndexConvention convention2 = (IborIndexConvention) conventionMaster.add(new ConventionDocument(
        new IborIndexConvention("Ibor2", conventionId2.toBundle(), DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
            2, false, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.financialRegionId("US"),
            ExternalSchemes.financialRegionId("US"), ""))).getConvention();
    final ConventionSource conventionSource = new MasterConventionSource(conventionMaster);
    final ConventionValidationInfo<IborIndexConvention> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, conventionSource);
    assertEquals(validationInfo.getUnsupportedConventions(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConventionIds(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getValidatedObjects(), Sets.newHashSet(convention1, convention2));
  }

  /**
   * Tests that duplicated conventions, i.e. multiple conventions with the same identifier, are identified.
   */
  @Test
  public void testDuplicatedConventions() {
    final ExternalId conventionId = ExternalId.of("CONVENTION", "Test");
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new IborCurveTypeConfiguration(conventionId, Tenor.THREE_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final IborIndexConvention convention = new IborIndexConvention("Ibor", conventionId.toBundle(), DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
        2, false, Currency.USD, LocalTime.of(11, 0), "US", ExternalSchemes.financialRegionId("US"), ExternalSchemes.financialRegionId("US"), "");
    final InMemoryConventionMaster conventionMaster = new InMemoryConventionMaster();
    conventionMaster.add(new ConventionDocument(convention));
    conventionMaster.add(new ConventionDocument(convention));
    final ConventionSource conventionSource = new MasterConventionSource(conventionMaster);
    final ConventionValidationInfo<IborIndexConvention> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, conventionSource);
    assertEquals(validationInfo.getUnsupportedConventions(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConventionIds(), Collections.singleton(conventionId));
  }

  /**
   * Tests that missing conventions are identified.
   */
  @Test
  public void testMissingConventions() {
    final ExternalId conventionId1 = ExternalId.of("CONVENTION", "Test1");
    final ExternalId conventionId2 = ExternalId.of("CONVENTION", "Test2");
    final List<? extends CurveTypeConfiguration> type1 = Arrays.asList(new IborCurveTypeConfiguration(conventionId1, Tenor.THREE_MONTHS),
        new IborCurveTypeConfiguration(conventionId2, Tenor.SIX_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type1));
    final ConventionValidationInfo<IborIndexConvention> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, EMPTY_CONVENTION_SOURCE);
    assertEquals(validationInfo.getUnsupportedConventions(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getMissingConventionIds(), Sets.newHashSet(conventionId1, conventionId2));
  }

  /**
   * Tests that conventions that are present in the master but not of the required type are identified.
   */
  @Test
  public void testUnsupportedConventions() {
    final ExternalId conventionId = ExternalId.of("CONVENTION", "Test");
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new IborCurveTypeConfiguration(conventionId, Tenor.THREE_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final InMemoryConventionMaster conventionMaster = new InMemoryConventionMaster();
    final OvernightIndexConvention convention = (OvernightIndexConvention) conventionMaster.add(new ConventionDocument(
        new OvernightIndexConvention("Convention", conventionId.toBundle(), DayCounts.ACT_360, 0, Currency.USD,
            ExternalSchemes.financialRegionId("US")))).getConvention();
    final ConventionSource conventionSource = new MasterConventionSource(conventionMaster);
    final ConventionValidationInfo<IborIndexConvention> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, conventionSource);
    assertEquals(validationInfo.getMissingConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConventionIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConventions(), Collections.singleton(convention));
  }
}
