/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.test.Assert;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityLoaderRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityLoaderRequestTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle IDS_1 = ExternalIdBundle.of(ExternalId.of("scheme1", "value1"), ExternalId.of("scheme1", "value2"));
  private static final ExternalIdBundle IDS_2 = ExternalIdBundle.of(ExternalId.of("scheme2", "value1"), ExternalId.of("scheme2", "value2"));

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIds() {
    new SecurityLoaderRequest().addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that the external id bundle array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdBundleArray() {
    new SecurityLoaderRequest().addExternalIds((ExternalIdBundle[]) null);
  }

  /**
   * Tests that the external id bundle object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdBundleIterable() {
    new SecurityLoaderRequest().addExternalIds((Iterable<ExternalIdBundle>) null);
  }

  /**
   * Tests that the request creation methods are equivalent.
   */
  @Test
  public void testCreate() {
    final SecurityLoaderRequest request1 = SecurityLoaderRequest.create(IDS_1);
    final SecurityLoaderRequest request2 = new SecurityLoaderRequest();
    request2.addExternalIds(IDS_1.getExternalIds().toArray(new ExternalId[0]));
    // each id is added as a separate bundle
    assertNotEquals(request1, request2);
    assertEquals(request2, SecurityLoaderRequest.create(Arrays.asList(ExternalIdBundle.of("scheme1", "value1"), ExternalIdBundle.of("scheme1", "value2"))));
    final SecurityLoaderRequest request3 = new SecurityLoaderRequest();
    request3.addExternalIds(IDS_1, IDS_2);
    request1.addExternalIds(IDS_2);
    assertEquals(request1, request3);
    final SecurityLoaderRequest request4 = SecurityLoaderRequest.create(ExternalIdBundle.of("scheme3", "value1"));
    request4.setExternalIdBundles(Sets.newHashSet(IDS_1, IDS_2));
    assertEquals(request1, request4);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SecurityLoaderRequest request = SecurityLoaderRequest.create(IDS_1);
    request.addExternalIds(IDS_2);
    Assert.assertEqualsNoOrder(request.getExternalIdBundles(), Arrays.asList(IDS_1, IDS_2));
    assertFalse(request.isForceUpdate());
    assertFalse(request.isReturnSecurityObjects());
    final SecurityLoaderRequest other = SecurityLoaderRequest.create(IDS_1);
    other.addExternalIds(IDS_2);
    assertEquals(request, request);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    assertEquals(request.toString(), "SecurityLoaderRequest{externalIdBundles=[Bundle[scheme1~value1, scheme1~value2], "
        + "Bundle[scheme2~value1, scheme2~value2]], forceUpdate=false, returnSecurityObjects=false}");
    assertNotEquals(null, request);
    assertNotEquals(IDS_1, request);
    other.setExternalIdBundles(Collections.singleton(IDS_1));
    assertNotEquals(request, other);
    other.addExternalIds(IDS_2);
    other.setForceUpdate(true);
    assertNotEquals(request, other);
    other.setForceUpdate(false);
    other.setReturnSecurityObjects(true);
    assertNotEquals(request, other);

  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SecurityLoaderRequest request = SecurityLoaderRequest.create(IDS_1);
    request.addExternalIds(IDS_2);
    request.setForceUpdate(true);
    Assert.assertEqualsNoOrder(request.metaBean().externalIdBundles().get(request), Arrays.asList(IDS_1, IDS_2));
    assertTrue(request.metaBean().forceUpdate().get(request));
    assertFalse(request.metaBean().returnSecurityObjects().get(request));
    Assert.assertEqualsNoOrder((Collection<?>) request.property("externalIdBundles").get(), Arrays.asList(IDS_1, IDS_2));
    assertEquals(request.property("forceUpdate").get(), true);
    assertEquals(request.property("returnSecurityObjects").get(), false);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecurityLoaderRequest request = SecurityLoaderRequest.create(IDS_1);
    request.addExternalIds(IDS_2);
    request.setForceUpdate(true);
    assertEncodeDecodeCycle(SecurityLoaderRequest.class, request);
  }
}
