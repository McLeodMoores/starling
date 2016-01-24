/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link NormalizerFactory}.
 */
public class NormalizerFactoryTest {

  /**
   * Tests the behaviour when a null name is supplied.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    NormalizerFactory.INSTANCE.of(null);
  }

  /**
   * Tests that normalizers can be added by scanning for annotations.
   */
  @Test
  public void testScanning() {
    final Map<String, Normalizer> instanceMap = NormalizerFactory.INSTANCE.instanceMap();
    assertNotNull(instanceMap);
    // want at least unit and /100 normalizers
    assertTrue(instanceMap.size() >= 2);
    assertTrue(instanceMap.containsKey(UnitNormalizer.INSTANCE.getName()));
    assertTrue(instanceMap.containsKey(Div100Normalizer.INSTANCE.getName()));
  }

}