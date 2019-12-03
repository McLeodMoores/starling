/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.mcleodmoores.web.json.convention.PriceIndexConventionJsonBuilder;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PriceIndexConventionJsonBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class PriceIndexConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final PriceIndexConvention convention = new PriceIndexConvention("UKCPI", externalIds, Currency.GBP, ExternalId.of("TEST", "LONDON"));
    convention.setAttributes(attributes);
    assertEquals(convention, PriceIndexConventionJsonBuilder.INSTANCE.fromJSON(PriceIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = PriceIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, PriceIndexConventionJsonBuilder.INSTANCE.toJSON(PriceIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final PriceIndexConvention convention = new PriceIndexConvention("UKCPI", externalIds, Currency.GBP, ExternalId.of("TEST", "LONDON"));
    final PriceIndexConvention copy = PriceIndexConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new PriceIndexConvention("UKCPI", externalIds, Currency.GBP, ExternalId.of("TEST", "LONDON")));
  }
}
