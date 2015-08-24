/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link IborCurveTypeSecurityValidator}.
 */
public class IborCurveTypeSecurityValidatorTest {
  /** An empty security source */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new MasterSecuritySource(new InMemorySecurityMaster());
  /** The validator */
  private static final SecurityValidator<CurveGroupConfiguration, IborIndex> VALIDATOR = IborCurveTypeSecurityValidator.getInstance();

  /**
   * Tests the behaviour when the curve group configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveGroupConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    VALIDATOR.validate(group, null, EMPTY_SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    VALIDATOR.validate(group, VersionCorrection.LATEST, (SecuritySource) null);
  }

  /**
   * Tests that other curve type configurations are not validated.
   */
  @Test
  public void testOvernightCurveType() {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new OvernightCurveTypeConfiguration(ExternalId.of("CONVENTION", "Test")));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final SecurityValidationInfo<IborIndex> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, EMPTY_SECURITY_SOURCE);
    assertEquals(validationInfo.getMissingSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedSecurities(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedSecurityIds(), Collections.emptySet());
  }

  /**
   * Tests that security that are of the expected type and uniquely available from the source are identified.
   */
  @Test
  public void testValidatedSecurities() {
    final ExternalId securityId1 = ExternalId.of("SECURITY", "Test1");
    final ExternalId securityId2 = ExternalId.of("SECURITY", "Test2");
    final List<? extends CurveTypeConfiguration> type = Arrays.asList(new IborCurveTypeConfiguration(securityId1, Tenor.THREE_MONTHS),
        new IborCurveTypeConfiguration(securityId2, Tenor.SIX_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    final IborIndex index1 = new IborIndex("Ibor1", Tenor.THREE_MONTHS, securityId1);
    index1.addExternalId(securityId1);
    final IborIndex index2 = new IborIndex("Ibor2", Tenor.SIX_MONTHS, securityId2);
    index2.addExternalId(securityId2);
    final IborIndex security1 = (IborIndex) securityMaster.add(new SecurityDocument(index1)).getSecurity();
    final IborIndex security2 = (IborIndex) securityMaster.add(new SecurityDocument(index2)).getSecurity();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final SecurityValidationInfo<IborIndex> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, securitySource);
    assertEquals(validationInfo.getUnsupportedSecurities(), Collections.emptySet());
    assertEquals(validationInfo.getMissingSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedSecurityIds(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getValidatedObjects(), Sets.newHashSet(security1, security2));
  }

  /**
   * Tests that duplicated securities, i.e. multiple securities with the same identifier, are identified.
   */
  @Test
  public void testDuplicatedSecurities() {
    final ExternalId securityId = ExternalId.of("SECURITY", "Test");
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new IborCurveTypeConfiguration(securityId, Tenor.THREE_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final IborIndex security = new IborIndex("Ibor", Tenor.THREE_MONTHS, securityId);
    security.addExternalId(securityId);
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    securityMaster.add(new SecurityDocument(security));
    securityMaster.add(new SecurityDocument(security));
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final SecurityValidationInfo<IborIndex> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, securitySource);
    assertEquals(validationInfo.getUnsupportedSecurities(), Collections.emptySet());
    assertEquals(validationInfo.getMissingSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedSecurityIds(), Collections.singleton(securityId));
  }

  /**
   * Tests that missing securities are identified.
   */
  @Test
  public void testMissingSecurities() {
    final ExternalId securityId1 = ExternalId.of("SECURITY", "Test1");
    final ExternalId securityId2 = ExternalId.of("SECURITY", "Test2");
    final List<? extends CurveTypeConfiguration> type1 = Arrays.asList(new IborCurveTypeConfiguration(securityId1, Tenor.THREE_MONTHS),
        new IborCurveTypeConfiguration(securityId2, Tenor.SIX_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type1));
    final SecurityValidationInfo<IborIndex> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, EMPTY_SECURITY_SOURCE);
    assertEquals(validationInfo.getUnsupportedSecurities(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getMissingSecurityIds(), Sets.newHashSet(securityId1, securityId2));
  }

  /**
   * Tests that securities that are present in the master but not of the required type are identified.
   */
  @Test
  public void testUnsupportedSecurities() {
    final ExternalId securityId = ExternalId.of("SECURITY", "Test");
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new IborCurveTypeConfiguration(securityId, Tenor.THREE_MONTHS));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    final OvernightIndex index = new OvernightIndex("Test", securityId);
    index.addExternalId(securityId);
    final OvernightIndex security = (OvernightIndex) securityMaster.add(new SecurityDocument(index)).getSecurity();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final SecurityValidationInfo<IborIndex> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, securitySource);
    assertEquals(validationInfo.getMissingSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedSecurityIds(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedObjects(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedSecurities(), Collections.singleton(security));
  }
}
