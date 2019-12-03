/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;


/**
 * Base interface for different kinds of market data keys.
 */
public interface StructuredMarketDataKey {
  /**
   * Visitor interface for working with the different key implementations.
   * @param <T> The return type of the visitor
   */
  public interface Visitor<T> {

    /**
     * Visits a yield curve key.
     *
     * @param key  the key
     * @return  the result
     */
    T visitYieldCurveKey(YieldCurveKey key);

    /**
     * Visits a volatility surface key.
     *
     * @param key  the key
     * @return  the result
     */
    T visitVolatilitySurfaceKey(VolatilitySurfaceKey key);

    /**
     * Visits a volatility cube key.
     *
     * @param key  the key
     * @return  the result
     */
    T visitVolatilityCubeKey(VolatilityCubeKey key);

    /**
     * Visits a curve key.
     *
     * @param curveKey  the key
     * @return  the result
     */
    T visitCurveKey(CurveKey curveKey);

    /**
     * Visits a surface key.
     *
     * @param surfaceKey  the key
     * @return  the result
     */
    T visitSurfaceKey(SurfaceKey surfaceKey);
  }

  /**
   * Applies the visitor to this key.
   *
   * @param <T> the return type of the visitor
   * @param visitor the visitor to apply, not null
   * @return the return value of the visitor
   */
  <T> T accept(Visitor<T> visitor);

}
