/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link SwapFixedLegConventionJsonBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedLegConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final SwapFixedLegConvention convention = new SwapFixedLegConvention("SWAP", externalIds, Tenor.SIX_MONTHS, DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING, Currency.AUD, ExternalId.of("TEST", "LONDON"), 2, true, StubType.LONG_END, true, 1);
    convention.setAttributes(attributes);
    assertEquals(convention, SwapFixedLegConventionJsonBuilder.INSTANCE.fromJSON(SwapFixedLegConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = SwapFixedLegConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, SwapFixedLegConventionJsonBuilder.INSTANCE.toJSON(SwapFixedLegConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final SwapFixedLegConvention convention = new SwapFixedLegConvention("SWAP", externalIds, Tenor.SIX_MONTHS, DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING, Currency.AUD, ExternalId.of("TEST", "LONDON"), 2, true, StubType.LONG_END, true, 1);
    final SwapFixedLegConvention copy = SwapFixedLegConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new SwapFixedLegConvention("SWAP", externalIds, Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
        Currency.AUD, ExternalId.of("TEST", "LONDON"), 2, true, StubType.LONG_END, true, 1));
  }
}
