/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalIdBundle;

/**
 * Unit tests for {@link BondConventionJsonBuilder}.
 */
public class BondConventionBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final BondConvention convention = new BondConvention("USGVT", externalIds, 7, 0, BusinessDayConventions.FOLLOWING, false, true);
    convention.setAttributes(attributes);
    assertEquals(convention, BondConventionJsonBuilder.INSTANCE.fromJSON(BondConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = BondConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, BondConventionJsonBuilder.INSTANCE.toJSON(BondConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final BondConvention convention = new BondConvention("USGVT", externalIds, 7, 0, BusinessDayConventions.FOLLOWING, false, true);
    final BondConvention copy = BondConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new BondConvention("USGVT", externalIds, 7, 0, BusinessDayConventions.FOLLOWING, false, true));
  }
}
