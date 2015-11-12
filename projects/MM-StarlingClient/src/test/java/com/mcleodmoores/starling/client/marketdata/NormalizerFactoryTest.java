package com.mcleodmoores.starling.client.marketdata;

import org.testng.annotations.Test;

/**
 * Created by jim on 08/06/15.
 */
public class NormalizerFactoryTest {
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    NormalizerFactory.INSTANCE.of(null);
  }
}