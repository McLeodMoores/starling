/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link SecurityValidationInfo}.
 */
public class SecurityValidationInfoTest {
  /** An overnight index */
  private static final OvernightIndex OVERNIGHT_INDEX_SECURITY = new OvernightIndex("ON", ExternalId.of("CONVENTION", "ON"));
  /** An ibor index */
  private static final IborIndex IBOR_INDEX_SECURITY_1 = new IborIndex("3M Libor", Tenor.THREE_MONTHS, ExternalId.of("CONVENTION", "3M Libor"));
  /** An ibor index */
  private static final IborIndex IBOR_INDEX_SECURITY_2 = new IborIndex("1M Libor", Tenor.ONE_MONTH, ExternalId.of("CONVENTION", "1M Libor"));
  /** Type of the validated securities */
  private static final Class<IborIndex> TYPE = IborIndex.class;
  /** The validated securities */
  private static final Collection<IborIndex> VALIDATED = new HashSet<>();
  /** Missing securities */
  private static final Collection<ExternalId> MISSING = new HashSet<>();
  /** Duplicated securities */
  private static final Collection<ExternalId> DUPLICATED = new HashSet<>();
  /** Unsupported securities */
  private static final Collection<Security> UNSUPPORTED = new HashSet<>();
  /** Security validation info */
  private static final SecurityValidationInfo<IborIndex> INFO;

  static {
    VALIDATED.add(IBOR_INDEX_SECURITY_1);
    VALIDATED.add(IBOR_INDEX_SECURITY_2);
    MISSING.add(ExternalSchemes.syntheticSecurityId("USD 6M Libor"));
    DUPLICATED.add(ExternalSchemes.syntheticSecurityId("USD 9M Libor"));
    UNSUPPORTED.add(OVERNIGHT_INDEX_SECURITY);
    INFO = new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType1() {
    new SecurityValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType2() {
    new SecurityValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the validated security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated1() {
    new SecurityValidationInfo<>(TYPE, null, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the validated security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated2() {
    new SecurityValidationInfo<>(TYPE, null, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the missing security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing1() {
    new SecurityValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED);
  }

  /**
   * Tests the behaviour when the missing security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing2() {
    new SecurityValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the duplicated security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated1() {
    new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, null);
  }

  /**
   * Tests the behaviour when the duplicated security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated2() {
    new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, null, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the unsupported security collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnsupported() {
    new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, null);
  }

  /**
   * Tests that the validated security collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValidatedSecurities() {
    final Collection<? super IborIndex> validated = INFO.getValidatedObjects();
    validated.add(VALIDATED.iterator().next());
  }

  /**
   * Tests that the missing security collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableMissingSecurities() {
    final Collection<ExternalId> missing = INFO.getMissingSecurityIds();
    missing.add(ExternalSchemes.syntheticSecurityId("USD 12M Libor"));
  }

  /**
   * Tests that the duplicated security collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableDuplicatedSecurities() {
    final Collection<ExternalId> duplicated = INFO.getDuplicatedSecurityIds();
    duplicated.add(ExternalSchemes.syntheticSecurityId("USD 3M Libor"));
  }

  /**
   * Tests that the unsupported securities collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableUnsupportedSecurities() {
    final Collection<Security> unsupported = INFO.getUnsupportedSecurities();
    unsupported.add(OVERNIGHT_INDEX_SECURITY);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Collection<Security> validated = new HashSet<>();
    validated.add(IBOR_INDEX_SECURITY_1);
    validated.add(IBOR_INDEX_SECURITY_2);
    final Collection<ExternalId> missing = new HashSet<>();
    missing.add(ExternalSchemes.syntheticSecurityId("USD 6M Libor"));
    final Collection<ExternalId> duplicated = new HashSet<>();
    duplicated.add(ExternalSchemes.syntheticSecurityId("USD 9M Libor"));
    final Collection<Security> unsupported = new HashSet<>();
    unsupported.add(OVERNIGHT_INDEX_SECURITY);
    assertEquals(INFO.getType(), TYPE);
    assertEquals(INFO.getValidatedObjects(), validated);
    assertEquals(INFO.getMissingSecurityIds(), missing);
    assertEquals(INFO.getDuplicatedSecurityIds(), duplicated);
    assertEquals(INFO.getUnsupportedSecurities(), unsupported);
    SecurityValidationInfo<? extends Security> other = new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
    assertEquals(other, INFO);
    assertEquals(other.hashCode(), INFO.hashCode());
    other = new SecurityValidationInfo<>(Security.class, Collections.singleton(IBOR_INDEX_SECURITY_1), MISSING, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new SecurityValidationInfo<>(TYPE, Collections.singleton(IBOR_INDEX_SECURITY_1), MISSING, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new SecurityValidationInfo<>(TYPE, VALIDATED, DUPLICATED, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, MISSING, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new SecurityValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, VALIDATED);
    assertNotEquals(other, INFO);
  }
}
