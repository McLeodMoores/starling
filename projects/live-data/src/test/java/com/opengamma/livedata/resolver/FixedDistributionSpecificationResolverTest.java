/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FixedDistributionSpecificationResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class FixedDistributionSpecificationResolverTest {
  private static final DistributionSpecification DIST_1 = new DistributionSpecification(ExternalId.of("eid", "1"), new NormalizationRuleSet("rule1"), "topic1");
  private static final DistributionSpecification DIST_2 = new DistributionSpecification(ExternalId.of("eid", "2"), new NormalizationRuleSet("rule2"), "topic2");
  private static final Map<LiveDataSpecification, DistributionSpecification> MAP;
  private static final List<LiveDataSpecification> SPECS = Arrays.asList(
      new LiveDataSpecification("rule1", Arrays.asList(ExternalId.of("eid", "1"))),
      new LiveDataSpecification("rule2", Arrays.asList(ExternalId.of("eid", "2"))),
      new LiveDataSpecification("rule3", Arrays.asList(ExternalId.of("eid", "3"))));
  static {
    MAP = new HashMap<>();
    MAP.put(SPECS.get(0), DIST_1);
    MAP.put(SPECS.get(1), DIST_2);
  }
  private static final FixedDistributionSpecificationResolver RESOLVER = new FixedDistributionSpecificationResolver(MAP);

  /**
   * Tests that the map cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdResolver() {
    new FixedDistributionSpecificationResolver(null);
  }

  /**
   * Tests that the live data specifications cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSpecifications() {
    RESOLVER.resolve((Collection<LiveDataSpecification>) null);
  }

  /**
   * Tests resolution of specifications.
   */
  public void testResolve() {
    final Map<LiveDataSpecification, DistributionSpecification> resolved = RESOLVER.resolve(SPECS);
    assertEquals(resolved.size(), SPECS.size());
    assertEquals(resolved.get(SPECS.get(0)), new DistributionSpecification(ExternalId.of("eid", "1"), new NormalizationRuleSet("rule1"), "topic1"));
    assertEquals(resolved.get(SPECS.get(1)), new DistributionSpecification(ExternalId.of("eid", "2"), new NormalizationRuleSet("rule2"), "topic2"));
    // no normalization rule for the id
    assertNull(resolved.get(SPECS.get(2)));
  }

}
