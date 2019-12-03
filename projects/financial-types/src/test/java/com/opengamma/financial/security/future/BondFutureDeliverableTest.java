/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link BondFutureDeliverable}.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureDeliverableTest extends AbstractBeanTestCase {
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("sec", "1");
  private static final double CONVERSION_FACTOR = 0.95;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BondFutureDeliverable.class, Arrays.asList("identifiers", "conversionFactor"), Arrays.asList(IDS, CONVERSION_FACTOR),
        Arrays.asList(ExternalIdBundle.of("sec", "2"), CONVERSION_FACTOR * 1.1));
  }

  /**
   * Tests the constructors.
   */
  public void testConstructor() {
    BondFutureDeliverable deliverable = new BondFutureDeliverable();
    assertTrue(deliverable.getIdentifiers().isEmpty());
    assertEquals(deliverable.getConversionFactor(), 0.);
    deliverable = new BondFutureDeliverable(IDS, CONVERSION_FACTOR);
    assertEquals(deliverable.getIdentifiers(), IDS);
    assertEquals(deliverable.getConversionFactor(), CONVERSION_FACTOR);
  }
}
