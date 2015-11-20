/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link ConfigurationValidationInfo}.
 */
public class ConfigurationValidationInfoTest {
  /** Constant curve configuration */
  private static final ConstantCurveDefinition CONSTANT_CURVE_1 = new ConstantCurveDefinition("Name 1", ExternalSchemes.syntheticSecurityId("ABC"));
  /** Constant curve configuration */
  private static final ConstantCurveDefinition CONSTANT_CURVE_2 = new ConstantCurveDefinition("Name 2", ExternalSchemes.syntheticSecurityId("DEF"));
  /** The type of the validated configurations */
  private static final Class<AbstractCurveDefinition> TYPE = AbstractCurveDefinition.class;
  /** The validated underlying configurations */
  private static final Collection<AbstractCurveDefinition> VALIDATED = new HashSet<>();
  /** The missing underlying configurations */
  private static final Map<String, Class<?>> MISSING = new HashMap<>();
  /** The duplicated underlying configurations */
  private static final Collection<Object> DUPLICATED = new HashSet<>();
  /** Configuration validation info */
  private static final ConfigurationValidationInfo<AbstractCurveDefinition> INFO;

  static {
    VALIDATED.add(CONSTANT_CURVE_1);
    VALIDATED.add(CONSTANT_CURVE_2);
    MISSING.put("Name 3", Currency.class);
    MISSING.put("Name 4", String.class);
    DUPLICATED.add("Name 5");
    INFO = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED);
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
    new ConfigurationValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED);
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
    new ConfigurationValidationInfo<>(TYPE, null, MISSING, DUPLICATED);
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
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED);
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
    new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, null);
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
    final Map<String, Class<?>> missing = INFO.getMissingConfigurations();
    missing.put("Name 10", AbstractCurveDefinition.class);
  }

  /**
   * Tests that the duplicated configurations collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableDuplicatedConfigurations() {
    final Collection<Object> duplicated = INFO.getDuplicatedConfigurations();
    duplicated.add(Currency.AUD);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Collection<AbstractCurveDefinition> validated = new HashSet<>();
    validated.add(CONSTANT_CURVE_1);
    validated.add(CONSTANT_CURVE_2);
    final Map<String, Class<?>> missing = new HashMap<>();
    missing.put("Name 3", Currency.class);
    missing.put("Name 4", String.class);
    final Collection<String> duplicated = new HashSet<>();
    duplicated.add("Name 5");
    assertEquals(INFO.getType(), TYPE);
    assertEquals(INFO.getValidatedConfigurations(), validated);
    assertEquals(INFO.getMissingConfigurations(), missing);
    assertEquals(INFO.getDuplicatedConfigurations(), duplicated);
    ConfigurationValidationInfo<AbstractCurveDefinition> other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED);
    assertEquals(other, INFO);
    assertEquals(other.hashCode(), INFO.hashCode());
    other = new ConfigurationValidationInfo<>(TYPE, Collections.<AbstractCurveDefinition>singleton(CONSTANT_CURVE_1), MISSING, DUPLICATED);
    assertNotEquals(other, INFO);
    other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, Collections.<String, Class<?>>singletonMap("Test", Currency.class), DUPLICATED);
    assertNotEquals(other, INFO);
    other = new ConfigurationValidationInfo<>(TYPE, VALIDATED, MISSING, Collections.<Object>emptySet());
    assertNotEquals(other, INFO);
  }

}
