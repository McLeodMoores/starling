/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link ToolContextConfigurationValidator}.
 */
public class ToolContextConfigurationValidatorTest {
  /** A config master */
  private static final InMemoryConfigMaster CONFIG_MASTER = new InMemoryConfigMaster();
  /** A config source */
  private static final ConfigSource CONFIG_SOURCE = new MasterConfigSource(CONFIG_MASTER);
  /** The underlying validator */
  private static final ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> UNDERLYING = DiscountingCurveTypeReferenceValidator.getInstance();
  /** The tool context validator */
  private static final ToolContextConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> VALIDATOR =
      ToolContextConfigurationValidator.of(UNDERLYING);
  /** The tool context */
  private static final ToolContext TOOL_CONTEXT = new ToolContext();

  static {
    TOOL_CONTEXT.setConfigSource(CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the underlying validator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    ToolContextConfigurationValidator.of(null);
  }

  /**
   * Tests the behaviour when the configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, TOOL_CONTEXT);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), null, TOOL_CONTEXT);
  }

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToolContext() {
    VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), VersionCorrection.LATEST, null);
  }

  /**
   * Tests the behaviour when the tool context does not have a config source set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoConfigSource() {
    try (ToolContext toolContext = new ToolContext()) {
      VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), toolContext);
    } catch (final Exception e) {
      throw e;
    }
  }

  /**
   * Tests that the wrapping is performed correctly.
   */
  @Test
  public void testValidate() {
    final String reference = "ABC";
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration(reference);
    assertEquals(VALIDATOR.validate(config, TOOL_CONTEXT), UNDERLYING.validate(config, VersionCorrection.LATEST, CONFIG_SOURCE));
    assertEquals(VALIDATOR.validate(config, VersionCorrection.LATEST, TOOL_CONTEXT), UNDERLYING.validate(config, VersionCorrection.LATEST, CONFIG_SOURCE));
  }
}
