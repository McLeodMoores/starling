/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.id.ExternalIdBundle;

/**
 * Unit tests for {@link FxSpotConventionJsonBuilder}.
 */
public class FxSpotConventionBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final FXSpotConvention convention = new FXSpotConvention("GBP/USD", externalIds, 2, true);
    convention.setAttributes(attributes);
    assertEquals(convention, FxSpotConventionJsonBuilder.INSTANCE.fromJSON(FxSpotConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = FxSpotConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, FxSpotConventionJsonBuilder.INSTANCE.toJSON(FxSpotConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final FXSpotConvention convention = new FXSpotConvention("GBP/USD", externalIds, 2, true);
    final FXSpotConvention copy = FxSpotConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new FXSpotConvention("GBP/USD", externalIds, 2, true));
  }
}
