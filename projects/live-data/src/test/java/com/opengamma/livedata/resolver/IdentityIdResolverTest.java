/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.resolver;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link IdentityIdResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class IdentityIdResolverTest {

  /**
   * Tests that the id bundle can only contain one id.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleIds() {
    new IdentityIdResolver().resolve(ExternalIdBundle.of(ExternalId.of("eid", "1"), ExternalId.of("eid", "2")));
  }

  /**
   * Tests that the identifier is returned unchanged.
   */
  public void testSingleId() {
    final ExternalId eid = ExternalId.of("eid", "1");
    final ExternalIdBundle eids = ExternalIdBundle.of(eid);
    assertEquals(new IdentityIdResolver().resolve(eids), eid);
  }

  /**
   * Tests that the identifiers are returned unchanged.
   */
  public void testIdCollection() {
    final ExternalId eid1 = ExternalId.of("eid", "1");
    final ExternalId eid2 = ExternalId.of("eid", "2");
    final ExternalId eid3 = ExternalId.of("eid", "3");
    final ExternalIdBundle eids1 = ExternalIdBundle.of(eid1);
    final ExternalIdBundle eids2 = ExternalIdBundle.of(eid2);
    final ExternalIdBundle eids3 = ExternalIdBundle.of(eid3);
    final Collection<ExternalIdBundle> eids = Arrays.asList(eids1, eids2, eids3);
    final Map<ExternalIdBundle, ExternalId> resolved = new IdentityIdResolver().resolve(eids);
    assertEquals(resolved.size(), eids.size());
    assertEquals(resolved.get(eids1), eid1);
    assertEquals(resolved.get(eids2), eid2);
    assertEquals(resolved.get(eids3), eid3);
  }
}
