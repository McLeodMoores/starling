/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.resolver;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link NaiveDistributionSpecificationResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class NaiveDistributionSpecificationResolverTest {
  private static final List<LiveDataSpecification> SPECS = Arrays.asList(
      new LiveDataSpecification("rule1", Arrays.asList(ExternalId.of("eid", "1"))),
      new LiveDataSpecification("rule2", Arrays.asList(ExternalId.of("eid", "2"))));
  private static final NaiveDistributionSpecificationResolver RESOLVER = new NaiveDistributionSpecificationResolver();

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
    assertEquals(resolved.get(SPECS.get(0)),
        new DistributionSpecification(ExternalId.of("eid", "1"), StandardRules.getNoNormalization(), ExternalIdBundle.of("eid", "1").toString()));
    assertEquals(resolved.get(SPECS.get(1)),
        new DistributionSpecification(ExternalId.of("eid", "2"), StandardRules.getNoNormalization(), ExternalIdBundle.of("eid", "2").toString()));
  }

}
