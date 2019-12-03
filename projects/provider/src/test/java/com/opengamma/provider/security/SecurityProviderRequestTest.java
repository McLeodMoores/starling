/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.security;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityProviderRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityProviderRequestTest extends AbstractBeanTestCase {
  private static final Set<ExternalIdBundle> IDS = new HashSet<>(Arrays.asList(ExternalIdBundle.of("eid1", "1"), ExternalIdBundle.of("eid2", "2")));
  private static final String DATA_SOURCE = "source";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SecurityProviderRequest.class, Arrays.asList("externalIdBundles", "dataSource"), Arrays.asList(IDS, DATA_SOURCE),
        Arrays.asList(new HashSet<>(Arrays.asList(ExternalIdBundle.of("eid10", "10"), ExternalIdBundle.of("eid20", "20"))), "other"));
  }

  /**
   * Tests that the create methods are equivalent for a single id bundle.
   */
  public void testCreateGetSingle() {
    final SecurityProviderRequest request = SecurityProviderRequest.createGet(IDS.iterator().next(), DATA_SOURCE);
    assertEquals(SecurityProviderRequest.createGet(Arrays.asList(IDS.iterator().next()), DATA_SOURCE), request);
  }

  /**
   * Tests that the identifiers array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdArray() {
    new SecurityProviderRequest().addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that the identifiers are added.
   */
  public void testAddExternalIds() {
    final ExternalId[] ids = new ExternalId[] { ExternalId.of("eid", "1"), ExternalId.of("eid", "2") };
    final SecurityProviderRequest request = new SecurityProviderRequest();
    request.addExternalIds(ids);
    assertEqualsNoOrder(request.getExternalIdBundles(), Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2")));
    final ExternalId[] moreIds = new ExternalId[] { ExternalId.of("eid", "10"), ExternalId.of("eid", "20") };
    request.addExternalIds(moreIds);
    assertEqualsNoOrder(request.getExternalIdBundles(),
        Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2"), ExternalIdBundle.of("eid", "10"), ExternalIdBundle.of("eid", "20")));
  }

  /**
   * Tests that the identifiers array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdBundleArray() {
    new SecurityProviderRequest().addExternalIds((ExternalIdBundle[]) null);
  }

  /**
   * Tests that the identifiers are added.
   */
  public void testAddExternalIdBundlesArray() {
    final ExternalIdBundle[] ids = new ExternalIdBundle[] { ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2") };
    final SecurityProviderRequest request = new SecurityProviderRequest();
    request.addExternalIds(ids);
    assertEqualsNoOrder(request.getExternalIdBundles(), Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2")));
    final ExternalIdBundle[] moreIds = new ExternalIdBundle[] { ExternalIdBundle.of("eid", "10"), ExternalIdBundle.of("eid", "20") };
    request.addExternalIds(moreIds);
    assertEqualsNoOrder(request.getExternalIdBundles(),
        Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2"), ExternalIdBundle.of("eid", "10"), ExternalIdBundle.of("eid", "20")));
  }

  /**
   * Tests that the identifiers iterable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdBundleIterable() {
    new SecurityProviderRequest().addExternalIds((Iterable<ExternalIdBundle>) null);
  }

  /**
   * Tests that the identifiers are added.
   */
  public void testAddExternalIdBundlesIterable() {
    final Iterable<ExternalIdBundle> ids = Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2"));
    final SecurityProviderRequest request = new SecurityProviderRequest();
    request.addExternalIds(ids);
    assertEqualsNoOrder(request.getExternalIdBundles(), Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2")));
    final Iterable<ExternalIdBundle> moreIds = Arrays.asList(ExternalIdBundle.of("eid", "10"), ExternalIdBundle.of("eid", "20"));
    request.addExternalIds(moreIds);
    assertEqualsNoOrder(request.getExternalIdBundles(),
        Arrays.asList(ExternalIdBundle.of("eid", "1"), ExternalIdBundle.of("eid", "2"), ExternalIdBundle.of("eid", "10"), ExternalIdBundle.of("eid", "20")));
  }
}
