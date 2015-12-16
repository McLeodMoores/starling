/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.classification.QuandlCodeClassifier;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;

/**
 * Unit tests for {@link QuandlCodeClassifier}.
 */
@Test
public class QuandlCodeClassifierTest {
  /** The classifier */
  private static final QuandlCodeClassifier CLASSIFIER = new QuandlCodeClassifier(CacheManager.newInstance());

  /**
   * Tests the behaviour when a null cache manager is supplied.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullCacheManager() {
    new QuandlCodeClassifier(null);
  }

  /**
   * Tests the normalization factors for rate futures.
   */
  @Test
  public void testKnownRateFutures() {
    final String[] quandlCodes = new String[] {"CME/EDZ2014", "CME/EMZ2014", "CME/EYZ2014", "CME/FFZ2014", "EUREX/FEO1Z2014",
        "EUREX/FEU3Z2014", "LIFFE/JZ2014", "LIFFE/IZ2014", "LIFFE/SZ2014", "LIFFE/EONZ2014", "MX/BAXZ2014", "SGX/EYZ2014",
        "SGX/EDZ2014", "SGX/ELZ2014", "TFX/JBAZ2014"};
    for (final String quandlCode : quandlCodes) {
      assertEquals(100, CLASSIFIER.getNormalizationFactor(quandlCode).intValue());
    }
  }

  /**
   * Tests the normalization factors for cash.
   */
  @Test
  public void testKnownCash() {
    final String[] quandlCodes = new String[] {"FRED/USD1MTD156N", "FRED/USD12MTD156N", "FRED/USDONTD156N"};
    for (final String quandlCode : quandlCodes) {
      assertEquals(100, CLASSIFIER.getNormalizationFactor(quandlCode).intValue());
    }
  }

  /**
   * Tests the normalization factors for swaps.
   */
  @Test
  public void testKnownSwap() {
    final String[] quandlCodes = new String[] {"FRED/DSWP1", "FRED/DSWP5", "FRED/DSWP10"};
    for (final String quandlCode : quandlCodes) {
      assertEquals(100, CLASSIFIER.getNormalizationFactor(quandlCode).intValue());
    }
  }

  /**
   * Tests that null is returned if the code could not be classified.
   */
  @Test
  public void testUnclassifiableCode() {
    assertNull(CLASSIFIER.getNormalizationFactor("ABC/DEF"));
  }

}
