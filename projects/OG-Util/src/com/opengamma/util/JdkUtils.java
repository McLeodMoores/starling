/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.math.BigDecimal;

/**
 * Utility to fix bugs in the JDK.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class JdkUtils {

  /** Singleton zero. */
  private static final BigDecimal ZERO = BigDecimal.valueOf(0, 0);

  /**
   * Restricted constructor.
   */
  private JdkUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Strips trailing zeros from a BigDecimal.
   * <p>
   * The JDK does not strip zeros from zero.
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539">Bug 6480539</a>.
   * 
   * @param decimal  the big decimal to strip, not null
   * @return the stripped decimal, not null
   */
  public static BigDecimal stripTrailingZeros(final BigDecimal decimal) {
    if (decimal.compareTo(ZERO) == 0) {
      return ZERO;
    } else {
      return decimal.stripTrailingZeros();
    }
  }

}
