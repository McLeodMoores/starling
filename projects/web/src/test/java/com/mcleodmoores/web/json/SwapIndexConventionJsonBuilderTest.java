/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Unit tests for {@link SwapIndexConventionJsonBuilder}.
 */
public class SwapIndexConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final ExternalId swapConvention = ExternalId.of("CONVENTION", "US SWAP");
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final SwapIndexConvention convention = new SwapIndexConvention("SWAP INDEX", externalIds, fixingTime, swapConvention);
    convention.setAttributes(attributes);
    assertEquals(convention, SwapIndexConventionJsonBuilder.INSTANCE.fromJSON(SwapIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = SwapIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, SwapIndexConventionJsonBuilder.INSTANCE.toJSON(SwapIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
  }

  /**
   * Tests the copy.
   */
  @Test
  public void testCopy() {
    final LocalTime fixingTime = LocalTime.of(11, 0);
    final ExternalId swapConvention = ExternalId.of("CONVENTION", "US SWAP");
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final SwapIndexConvention convention = new SwapIndexConvention("SWAP INDEX", externalIds, fixingTime, swapConvention);
    final SwapIndexConvention copy = SwapIndexConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new SwapIndexConvention("SWAP INDEX", externalIds, fixingTime, swapConvention));
  }
}
