/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CompilationContextSecurityValidator}.
 */
public class CompilationContextSecurityValidatorTest {
  /** A security master */
  private static final InMemorySecurityMaster SECURITY_MASTER = new InMemorySecurityMaster();
  /** A security source */
  private static final SecuritySource SECURITY_SOURCE = new MasterSecuritySource(SECURITY_MASTER);
  /** The underlying validator */
  private static final SecurityValidator<CurveGroupConfiguration, IborIndex> UNDERLYING = IborCurveTypeSecurityValidator.getInstance();
  /** The compilation context validator */
  private static final CompilationContextSecurityValidator<CurveGroupConfiguration, IborIndex> VALIDATOR = CompilationContextSecurityValidator.of(UNDERLYING);
  /** The compilation context */
  private static final FunctionCompilationContext COMPILATION_CONTEXT = new FunctionCompilationContext();
  /** The object to be validated */
  private static final CurveGroupConfiguration GROUP;

  static {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(
        new IborCurveTypeConfiguration(ExternalId.of("SECURITY", "Test"), Tenor.THREE_MONTHS));
    GROUP = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
  }

  static {
    COMPILATION_CONTEXT.put(OpenGammaCompilationContext.SECURITY_SOURCE_NAME, SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the underlying validator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    CompilationContextSecurityValidator.of(null);
  }

  /**
   * Tests the behaviour when the convention is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, COMPILATION_CONTEXT);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    VALIDATOR.validate(GROUP, null, COMPILATION_CONTEXT);
  }

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCompilationContext() {
    VALIDATOR.validate(GROUP, VersionCorrection.LATEST, null);
  }

  /**
   * Tests the behaviour when the tool context does not have a security source set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoSecuritySource() {
    VALIDATOR.validate(GROUP, new FunctionCompilationContext());
  }

  /**
   * Tests that the wrapping is performed correctly.
   */
  @Test
  public void testValidate() {
    assertEquals(VALIDATOR.validate(GROUP, COMPILATION_CONTEXT), UNDERLYING.validate(GROUP, VersionCorrection.LATEST, SECURITY_SOURCE));
    assertEquals(VALIDATOR.validate(GROUP, VersionCorrection.LATEST, COMPILATION_CONTEXT),
        UNDERLYING.validate(GROUP, VersionCorrection.LATEST, SECURITY_SOURCE));
  }
}
