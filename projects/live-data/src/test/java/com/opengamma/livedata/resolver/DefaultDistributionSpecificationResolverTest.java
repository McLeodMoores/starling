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
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DefaultDistributionSpecificationResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultDistributionSpecificationResolverTest {
  private static final IdResolver ID_RESOLVER = new IdentityIdResolver();
  private static final NormalizationRuleResolver NORMALIZATION_RESOLVER = new StandardRuleResolver(
      Arrays.asList(new NormalizationRuleSet("rule1"), new NormalizationRuleSet("rule2")));
  private static final JmsTopicNameResolver REQUEST = IdentityJmsNameResolver.INSTANCE;
  private static final DefaultDistributionSpecificationResolver RESOLVER = new DefaultDistributionSpecificationResolver(ID_RESOLVER, NORMALIZATION_RESOLVER, REQUEST);
  private static final List<LiveDataSpecification> SPECS = Arrays.asList(
      new LiveDataSpecification("rule1", Arrays.asList(ExternalId.of("eid", "1"))),
      new LiveDataSpecification("rule2", Arrays.asList(ExternalId.of("eid", "2"))),
      new LiveDataSpecification("rule3", Arrays.asList(ExternalId.of("eid", "3"))));

  /**
   * Tests that the id resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdResolver() {
    new DefaultDistributionSpecificationResolver(null, NORMALIZATION_RESOLVER, REQUEST);
  }

  /**
   * Tests that the normalization rule resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNormalizationRuleResolver() {
    new DefaultDistributionSpecificationResolver(ID_RESOLVER, null, REQUEST);
  }

  /**
   * Tests that the topic name resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTopicNameResolver() {
    new DefaultDistributionSpecificationResolver(ID_RESOLVER, NORMALIZATION_RESOLVER, null);
  }

  /**
   * Tests that the live data specifications cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpecifications() {
    RESOLVER.resolve((Collection<LiveDataSpecification>) null);
  }

  /**
   * Tests resolution of specifications.
   */
  public void testResolve() {
    final Map<LiveDataSpecification, DistributionSpecification> resolved = RESOLVER.resolve(SPECS);
    assertEquals(resolved.size(), SPECS.size());
    assertEquals(resolved.get(SPECS.get(0)), new DistributionSpecification(ExternalId.of("eid", "1"), new NormalizationRuleSet("rule1"), "topic:eid~1"));
    assertEquals(resolved.get(SPECS.get(1)), new DistributionSpecification(ExternalId.of("eid", "2"), new NormalizationRuleSet("rule2"), "topic:eid~2"));
    // no normalization rule for the id
    assertNull(resolved.get(SPECS.get(2)));
  }

  private static class IdentityJmsNameResolver implements JmsTopicNameResolver {
    private static final IdentityJmsNameResolver INSTANCE = new IdentityJmsNameResolver();

    private IdentityJmsNameResolver() {
    }

    @Override
    public String resolve(final JmsTopicNameResolveRequest request) {
      return "topic:" + request.getMarketDataUniqueId().toString();
    }

    @Override
    public Map<JmsTopicNameResolveRequest, String> resolve(final Collection<JmsTopicNameResolveRequest> requests) {
      final Map<JmsTopicNameResolveRequest, String> resolved = new HashMap<>();
      for (final JmsTopicNameResolveRequest request : requests) {
        resolved.put(request, resolve(request));
      }
      return resolved;
    }

  }
}
