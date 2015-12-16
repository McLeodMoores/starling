/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuandlSecurityTypeResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlSecurityTypeResolverTest {
  /** The resolver */
  private static final QuandlSecurityTypeResolver RESOLVER = new QuandlSecurityTypeResolver();

  /**
   * Tests identification of rate future codes.
   */
  @Test
  public void testIrFuture() {
    Set<ExternalIdBundle> idBundles = Collections.singleton(ExternalIdBundle.of(ExternalId.of("Test", "EDZ4")));
    Map<ExternalIdBundle, QuandlSecurityType> securityTypes = RESOLVER.getSecurityType(idBundles);
    assertTrue(securityTypes.isEmpty());
    idBundles = Collections.singleton(ExternalIdBundle.of(QuandlConstants.QUANDL_CODE, "CME/ABZ2014"));
    securityTypes = RESOLVER.getSecurityType(idBundles);
    assertTrue(securityTypes.isEmpty());
    idBundles = Collections.singleton(ExternalIdBundle.of(ExternalId.of(QuandlConstants.QUANDL_CODE, "CME/EDZ2014"),
        ExternalId.of("Test", "EDZ4")));
    securityTypes = RESOLVER.getSecurityType(idBundles);
    assertEquals(1, securityTypes.size());
    assertEquals(idBundles.iterator().next(), securityTypes.keySet().iterator().next());
    assertEquals(QuandlSecurityType.RATE_FUTURE, securityTypes.values().iterator().next());
  }

}
