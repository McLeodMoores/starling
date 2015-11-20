/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.config.ToolContextConfigurationValidator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.convention.impl.MasterConventionSource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link ToolContextConventionValidator}.
 */
public class ToolContextConventionValidatorTest {
  /** A convention master */
  private static final InMemoryConventionMaster CONVENTION_MASTER = new InMemoryConventionMaster();
  /** A convention source */
  private static final ConventionSource CONVENTION_SOURCE = new MasterConventionSource(CONVENTION_MASTER);
  /** The underlying validator */
  private static final ConventionValidator<CurveGroupConfiguration, IborIndexConvention> UNDERLYING = IborCurveTypeConventionValidator.getInstance();
  /** The tool context validator */
  private static final ToolContextConventionValidator<CurveGroupConfiguration, IborIndexConvention> VALIDATOR =
      ToolContextConventionValidator.of(UNDERLYING);
  /** The tool context */
  private static final ToolContext TOOL_CONTEXT = new ToolContext();
  /** The object to be validated */
  private static final CurveGroupConfiguration GROUP;

  static {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(
        new IborCurveTypeConfiguration(ExternalId.of("CONVENTION", "Test"), Tenor.THREE_MONTHS));
    GROUP = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    TOOL_CONTEXT.setConventionSource(CONVENTION_SOURCE);
  }

  /**
   * Tests the behaviour when the underlying validator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    ToolContextConfigurationValidator.of(null);
  }

  /**
   * Tests the behaviour when the convention is null.
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
    VALIDATOR.validate(GROUP, null, TOOL_CONTEXT);
  }

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToolContext() {
    VALIDATOR.validate(GROUP, VersionCorrection.LATEST, null);
  }

  /**
   * Tests the behaviour when the tool context does not have a convention source set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoConventionSource() {
    try (ToolContext toolContext = new ToolContext()) {
      VALIDATOR.validate(GROUP, toolContext);
    } catch (final Exception e) {
      throw e;
    }
  }

  /**
   * Tests that the wrapping is performed correctly.
   */
  @Test
  public void testValidate() {
    assertEquals(VALIDATOR.validate(GROUP, TOOL_CONTEXT), UNDERLYING.validate(GROUP, VersionCorrection.LATEST, CONVENTION_SOURCE));
    assertEquals(VALIDATOR.validate(GROUP, VersionCorrection.LATEST, TOOL_CONTEXT), UNDERLYING.validate(GROUP, VersionCorrection.LATEST, CONVENTION_SOURCE));
  }
}
