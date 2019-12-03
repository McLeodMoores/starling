/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

/**
 * Tests visitor for structured market data keys.
 */
public final class TestKeyVisitor implements StructuredMarketDataKey.Visitor<String> {

  /**
   * An instance.
   */
  public static final StructuredMarketDataKey.Visitor<String> INSTANCE = new TestKeyVisitor();

  @Override
  public String visitYieldCurveKey(final YieldCurveKey key) {
    return key.getClass().getSimpleName();
  }

  @Override
  public String visitVolatilitySurfaceKey(final VolatilitySurfaceKey key) {
    return key.getClass().getSimpleName();
  }

  @Override
  public String visitVolatilityCubeKey(final VolatilityCubeKey key) {
    return key.getClass().getSimpleName();
  }

  @Override
  public String visitCurveKey(final CurveKey curveKey) {
    return curveKey.getClass().getSimpleName();
  }

  @Override
  public String visitSurfaceKey(final SurfaceKey surfaceKey) {
    return surfaceKey.getClass().getSimpleName();
  }

  private TestKeyVisitor() {
  }
}
