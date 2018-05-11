/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link OvernightIndexConventionJsonBuilder}.
 */
public class OvernightIndexConventionBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final OvernightIndexConvention convention = new OvernightIndexConvention("O/N", externalIds,
        DayCounts.ACT_360, 2, Currency.AUD, ExternalId.of("TEST", "LONDON"));
    convention.setAttributes(attributes);
    assertEquals(convention, OvernightIndexConventionJsonBuilder.INSTANCE.fromJSON(OvernightIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = OvernightIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, OvernightIndexConventionJsonBuilder.INSTANCE.toJSON(OvernightIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
  }
}
