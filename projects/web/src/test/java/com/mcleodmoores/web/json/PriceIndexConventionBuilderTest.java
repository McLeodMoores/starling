/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link PriceIndexConventionJsonBuilder}.
 */
public class PriceIndexConventionBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final PriceIndexConvention convention = new PriceIndexConvention("O/N", externalIds,
        Currency.AUD, ExternalId.of("TEST", "LONDON"));
    convention.setAttributes(attributes);
    assertEquals(convention, PriceIndexConventionJsonBuilder.INSTANCE.fromJSON(PriceIndexConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = PriceIndexConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, PriceIndexConventionJsonBuilder.INSTANCE.toJSON(PriceIndexConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
  }
}
