/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Unit tests for {@link FxForwardAndSwapConventionJsonBuilder}.
 */
public class FxForwardAndSwapConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final FXForwardAndSwapConvention convention = new FXForwardAndSwapConvention("GBP/USD", externalIds, ExternalId.of("CONVENTION", "SPOT"),
        BusinessDayConventions.FOLLOWING, true);
    convention.setAttributes(attributes);
    assertEquals(convention, FxForwardAndSwapConventionJsonBuilder.INSTANCE.fromJSON(FxForwardAndSwapConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = FxForwardAndSwapConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson,
        FxForwardAndSwapConventionJsonBuilder.INSTANCE.toJSON(FxForwardAndSwapConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
  }

  /**
   * Tests the copy.
   */
  @Test
  public void testCopy() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final FXForwardAndSwapConvention convention = new FXForwardAndSwapConvention("GBP/USD", externalIds, ExternalId.of("CONVENTION", "SPOT"),
        BusinessDayConventions.FOLLOWING, true);
    final FXForwardAndSwapConvention copy = FxForwardAndSwapConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new FXForwardAndSwapConvention("GBP/USD", externalIds, ExternalId.of("CONVENTION", "SPOT"),
        BusinessDayConventions.FOLLOWING, true));
  }
}
