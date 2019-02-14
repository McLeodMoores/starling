/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link JmsTopicNameResolveRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class JmsTopicNameResolveRequestTest {
  private static final ExternalId MARKET_DATA_ID = ExternalId.of("eid", "1");
  private static final NormalizationRuleSet NORMALIZATION_RULE = new NormalizationRuleSet("id");

  /**
   * Tests the request.
   */
  public void testObject() {
    final JmsTopicNameResolveRequest request = new JmsTopicNameResolveRequest(MARKET_DATA_ID, NORMALIZATION_RULE);
    assertEquals(request.getMarketDataUniqueId(), MARKET_DATA_ID);
    assertEquals(request.getNormalizationRule(), NORMALIZATION_RULE);
    assertEquals(request, request);
    assertNotEquals(MARKET_DATA_ID, request);
    JmsTopicNameResolveRequest other = new JmsTopicNameResolveRequest(MARKET_DATA_ID, NORMALIZATION_RULE);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other = new JmsTopicNameResolveRequest(null, NORMALIZATION_RULE);
    assertNotEquals(request, other);
    other = new JmsTopicNameResolveRequest(MARKET_DATA_ID, null);
    assertNotEquals(request, other);
  }
}
