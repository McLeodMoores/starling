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

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link IborIndexConventionJsonBuilder}.
 */
public class IborIndexConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final IborIndexConvention convention = new IborIndexConvention("IBOR", externalIds,
        DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.AUD, LocalTime.of(11, 0),
        "LONDON", ExternalId.of("TEST", "LONDON"), ExternalId.of("TEST", "NY"), "");
    convention.setAttributes(attributes);
    assertEquals(convention, IborIndexConventionJsonBuilder.INSTANCE.fromJSON(IborIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = IborIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, IborIndexConventionJsonBuilder.INSTANCE.toJSON(IborIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
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
    final IborIndexConvention convention = new IborIndexConvention("IBOR", externalIds,
        DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.AUD, LocalTime.of(11, 0),
        "LONDON", ExternalId.of("TEST", "LONDON"), ExternalId.of("TEST", "NY"), "");
    final IborIndexConvention copy = IborIndexConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new IborIndexConvention("IBOR", externalIds,
        DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.AUD, LocalTime.of(11, 0),
        "LONDON", ExternalId.of("TEST", "LONDON"), ExternalId.of("TEST", "NY"), ""));
  }
}
