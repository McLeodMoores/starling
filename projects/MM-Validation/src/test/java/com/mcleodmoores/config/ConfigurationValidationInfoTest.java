/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.testng.annotations.Test;

import com.mcleodmoores.config.ConfigurationValidationInfo;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;

/**
 * Unit tests for {@link ConfigurationValidationInfo}.
 */
public class ConfigurationValidationInfoTest {
  /** Constant curve configuration */
  private static final ConstantCurveDefinition CONSTANT_CURVE_1 = new ConstantCurveDefinition("Name 1", ExternalSchemes.syntheticSecurityId("ABC"));
  /** Constant curve configuration */
  private static final ConstantCurveDefinition CONSTANT_CURVE_2 = new ConstantCurveDefinition("Name 2", ExternalSchemes.syntheticSecurityId("DEF"));
  /** A currency matrix configuration */
  private static final CurrencyMatrix MATRIX = new SimpleCurrencyMatrix();
  /** The type of the validated configurations */
  private static final Class<AbstractCurveDefinition> TYPE = AbstractCurveDefinition.class;
  /** The validated underlying configurations */
  private static final Collection<AbstractCurveDefinition> VALIDATED = new HashSet<>();
  /** The missing underlying configurations */
  private static final Collection<String> MISSING = new HashSet<>();
  /** The duplicated underlying configurations */
  private static final Collection<String> DUPLICATED = new HashSet<>();
  /** The unsupported underlying configurations */
  private static final Collection<? super Object> UNSUPPORTED = new HashSet<>();
  /** Configuration validation info */
  private static final ConfigurationValidationInfo<AbstractCurveDefinition> INFO;

  static {
    VALIDATED.add(CONSTANT_CURVE_1);
    VALIDATED.add(CONSTANT_CURVE_2);
    MISSING.add("Name 3");
    MISSING.add("Name 4");
    DUPLICATED.add("Name 5");
    UNSUPPORTED.add(MATRIX);
    INFO = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType1() {
    new ConfigurationValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType2() {
    new ConfigurationValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the validated configuration collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated1() {
    new ConfigurationValidationInfo<>(TYPE, null, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the validated configuration collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated2() {
    new ConfigurationValidationInfo<>(TYPE, null, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the missing configurations collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing1() {
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED);
  }

  /**
   * Tests the behaviour when the missing configurations collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing2() {
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the duplicated configurations collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated1() {
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, null);
  }

  /**
   * Tests the behaviour when the duplicated configurations collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated2() {
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, null, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the unsupported configurations collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnsupported() {
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, null);
  }

  /**
   * Tests that the validated configurations collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValidatedConfigurations() {
    final Collection<AbstractCurveDefinition> validated = INFO.getValidatedConfigurations();
    validated.add(VALIDATED.iterator().next());
  }

  /**
   * Tests that the missing configurations collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableMissingConfigurations() {
    final Collection<String> missing = INFO.getMissingConfigurationNames();
    missing.add("Name 10");
  }

  /**
   * Tests that the duplicated configurations collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableDuplicatedConfigurations() {
    final Collection<String> duplicated = INFO.getDuplicatedConfigurationNames();
    duplicated.add("Name 10");
  }

  /**
   * Tests that the unsupported configurations collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableUnsupportedConfigurations() {
    final Collection<Object> unsupported = (Collection<Object>) INFO.getUnsupportedConfigurations();
    unsupported.add(MATRIX);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Collection<AbstractCurveDefinition> validated = new HashSet<>();
    validated.add(CONSTANT_CURVE_1);
    validated.add(CONSTANT_CURVE_2);
    final Collection<String> missing = new HashSet<>();
    missing.add("Name 3");
    missing.add("Name 4");
    final Collection<String> duplicated = new HashSet<>();
    duplicated.add("Name 5");
    final Collection<Object> unsupported = new HashSet<>();
    unsupported.add(MATRIX);
    assertEquals(INFO.getType(), TYPE);
    assertEquals(INFO.getValidatedConfigurations(), validated);
    assertEquals(INFO.getMissingConfigurationNames(), missing);
    assertEquals(INFO.getDuplicatedConfigurationNames(), duplicated);
    assertEquals(INFO.getUnsupportedConfigurations(), unsupported);
    ConfigurationValidationInfo<AbstractCurveDefinition> other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
    assertEquals(other, INFO);
    assertEquals(other.hashCode(), INFO.hashCode());
    other = new ConfigurationValidationInfo<>(TYPE, Collections.singleton(CONSTANT_CURVE_1), MISSING, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, DUPLICATED, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, MISSING, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, VALIDATED);
    assertNotEquals(other, INFO);
  }
}
