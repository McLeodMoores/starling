/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link DiscountingCurveTypeReferenceValidator}.
 */
public class DiscountingCurveTypeReferenceValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> VALIDATOR = DiscountingCurveTypeReferenceValidator.getInstance();

  /**
   * Tests the behaviour when the curve construction configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveConstructionConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration("ABC");
    VALIDATOR.validate(config, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration("ABC");
    VALIDATOR.validate(config, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests that unsupported currencies (i.e. reference strings that cannot be parsed as currencies) are correctly identified.
   */
  @Test
  public void testUnsupportedCurrencies() {
    final String reference = "ABCD";
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration(reference);
    final ConfigurationValidationInfo<Currency> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.singleton(reference));
  }

  /**
   * Tests that supported currencies are correctly identified.
   */
  @Test
  public void testSupportedCurrencies() {
    final String reference = "ABC";
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration(reference);
    final ConfigurationValidationInfo<Currency> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(Currency.of(reference)));
  }
}
