/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link CompilationContextConfigurationValidator}.
 */
public class CompilationContextConfigurationValidatorTest {
  /** A config master */
  private static final InMemoryConfigMaster CONFIG_MASTER = new InMemoryConfigMaster();
  /** A config source */
  private static final ConfigSource CONFIG_SOURCE = new MasterConfigSource(CONFIG_MASTER);
  /** The underlying validator */
  private static final ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> UNDERLYING = DiscountingCurveTypeReferenceValidator.getInstance();
  /** The compilation context validator */
  private static final CompilationContextConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> VALIDATOR =
      CompilationContextConfigurationValidator.of(UNDERLYING);
  /** The compilation context */
  private static final FunctionCompilationContext COMPILATION_CONTEXT = new FunctionCompilationContext();

  static {
    COMPILATION_CONTEXT.put(OpenGammaCompilationContext.CONFIG_SOURCE_NAME, CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the underlying validator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    CompilationContextConfigurationValidator.of(null);
  }

  /**
   * Tests the behaviour when the configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, COMPILATION_CONTEXT);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), null, COMPILATION_CONTEXT);
  }

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCompilationContext() {
    VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), VersionCorrection.LATEST, null);
  }

  /**
   * Tests the behaviour when the tool context does not have a config source set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoConfigSource() {
    VALIDATOR.validate(new DiscountingCurveTypeConfiguration("ABC"), new FunctionCompilationContext());
  }

  /**
   * Tests that the wrapping is performed correctly.
   */
  @Test
  public void testValidate() {
    final String reference = "ABC";
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration(reference);
    assertEquals(VALIDATOR.validate(config, COMPILATION_CONTEXT), UNDERLYING.validate(config, VersionCorrection.LATEST, CONFIG_SOURCE));
    assertEquals(VALIDATOR.validate(config, VersionCorrection.LATEST, COMPILATION_CONTEXT),
        UNDERLYING.validate(config, VersionCorrection.LATEST, CONFIG_SOURCE));
  }
}
