/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link ToolContextSecurityValidator}.
 */
public class ToolContextSecurityValidatorTest {
  /** A security master */
  private static final InMemorySecurityMaster SECURITY_MASTER = new InMemorySecurityMaster();
  /** A security source */
  private static final SecuritySource SECURITY_SOURCE = new MasterSecuritySource(SECURITY_MASTER);
  /** The underlying validator */
  private static final SecurityValidator<CurveGroupConfiguration, IborIndex> UNDERLYING = IborCurveTypeSecurityValidator.getInstance();
  /** The tool context validator */
  private static final ToolContextSecurityValidator<CurveGroupConfiguration, IborIndex> VALIDATOR = ToolContextSecurityValidator.of(UNDERLYING);
  /** The tool context */
  private static final ToolContext TOOL_CONTEXT = new ToolContext();
  /** The object to be validated */
  private static final CurveGroupConfiguration GROUP;

  static {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(
        new IborCurveTypeConfiguration(ExternalId.of("SECURITY", "Test"), Tenor.THREE_MONTHS));
    GROUP = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    TOOL_CONTEXT.setSecuritySource(SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the underlying validator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    ToolContextSecurityValidator.of(null);
  }

  /**
   * Tests the behaviour when the security is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity() {
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
   * Tests the behaviour when the tool context does not have a security source set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoSecuritySource() {
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
    assertEquals(VALIDATOR.validate(GROUP, TOOL_CONTEXT), UNDERLYING.validate(GROUP, VersionCorrection.LATEST, SECURITY_SOURCE));
    assertEquals(VALIDATOR.validate(GROUP, VersionCorrection.LATEST, TOOL_CONTEXT), UNDERLYING.validate(GROUP, VersionCorrection.LATEST, SECURITY_SOURCE));
  }
}
