/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link OvernightIndexConventionJsonBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class OvernightIndexConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final OvernightIndexConvention convention = new OvernightIndexConvention("O/N", externalIds, DayCounts.ACT_360, 2, Currency.AUD,
        ExternalId.of("TEST", "LONDON"));
    convention.setAttributes(attributes);
    assertEquals(convention, OvernightIndexConventionJsonBuilder.INSTANCE.fromJSON(OvernightIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = OvernightIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, OvernightIndexConventionJsonBuilder.INSTANCE.toJSON(OvernightIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final OvernightIndexConvention convention = new OvernightIndexConvention("O/N", externalIds, DayCounts.ACT_360, 2, Currency.AUD,
        ExternalId.of("TEST", "LONDON"));
    final OvernightIndexConvention copy = OvernightIndexConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new OvernightIndexConvention("O/N", externalIds, DayCounts.ACT_360, 2, Currency.AUD, ExternalId.of("TEST", "LONDON")));
  }
}
