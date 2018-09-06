/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;


import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link LinkUtils}.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class LinkUtilsTest {

  private static final ObjectId OBJECT_ID = ObjectId.of("A", "B");
  private static final ExternalIdBundle EXTERNAL_ID_BUNDLE = ExternalIdBundle.of("C", "D");

  /**
   * Tests that the link cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLinkForBest() {
    LinkUtils.best(null);
  }

  /**
   * Tests that a link with no object id will return the id bundle.
   */
  @Test
  public void testBestEmpty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertEquals(ExternalIdBundle.EMPTY, LinkUtils.best(test));
  }

  /**
   * Tests that the object id is returned.
   */
  @Test
  public void testBestObjectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setObjectId(OBJECT_ID);
    assertEquals(OBJECT_ID, LinkUtils.best(test));
  }

  /**
   * Tests that the bundle is returned.
   */
  @Test
  public void testBestExternalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertEquals(EXTERNAL_ID_BUNDLE, LinkUtils.best(test));
  }

  /**
   * Tests that the object id is returned in preference to the external ids.
   */
  @Test
  public void testBestBothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    test.setObjectId(OBJECT_ID);
    assertEquals(OBJECT_ID, LinkUtils.best(test));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the best name wen the link is empty.
   */
  @Test
  public void testBestNameEmpty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertEquals("", LinkUtils.bestName(test));
  }

  /**
   * Tests that the object id is returned if there are no external ids.
   */
  @Test
  public void testBestNameObjectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setObjectId(OBJECT_ID);
    assertEquals("A~B", LinkUtils.bestName(test));
  }

  /**
   * Tests that the value of the external id is returned.
   */
  @Test
  public void testBestNameExternalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertEquals("D", LinkUtils.bestName(test));
  }

  /**
   * Tests the value when the external id bundle is empty.
   */
  @Test
  public void testBestNameEmptyExternalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(ExternalIdBundle.EMPTY);
    assertEquals("", LinkUtils.bestName(test));
  }

  /**
   * Tests that the external id is returned in preference to the object id.
   */
  @Test
  public void testBestNameBothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setObjectId(OBJECT_ID);
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    assertEquals("D", LinkUtils.bestName(test));
  }

  /**
   * Tests that the link cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBestNameNull() {
    assertNull(LinkUtils.bestName(null));
  }

  /**
   * Tests the order of valid links.
   */
  @Test
  public void testBestNameOrder() {
    ExternalIdBundle bundle = ExternalIdBundle.of(ExternalSchemes.BLOOMBERG_TICKER, "A");
    bundle = bundle.withExternalId(ExternalId.of(ExternalSchemes.RIC, "B"));
    bundle = bundle.withExternalId(ExternalId.of(ExternalSchemes.ACTIVFEED_TICKER, "C"));
    bundle = bundle.withExternalId(ExternalId.of("TEST", "D"));
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setExternalId(bundle);
    assertEquals("A", LinkUtils.bestName(test));
    bundle = bundle.withoutScheme(ExternalSchemes.BLOOMBERG_TICKER);
    test.setExternalId(bundle);
    assertEquals("B", LinkUtils.bestName(test));
    bundle = bundle.withoutScheme(ExternalSchemes.RIC);
    test.setExternalId(bundle);
    assertEquals("C", LinkUtils.bestName(test));
    bundle = bundle.withoutScheme(ExternalSchemes.ACTIVFEED_TICKER);
    test.setExternalId(bundle);
    assertEquals("D", LinkUtils.bestName(test));
    bundle = bundle.withoutScheme(ExternalScheme.of("TEST"));
    test.setExternalId(bundle);
    assertEquals("", LinkUtils.bestName(test));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the result when the link is empty.
   */
  @Test
  public void testIsValidEmpty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertFalse(LinkUtils.isValid(test));
  }

  /**
   * Tests the result when the link contains an object id.
   */
  @Test
  public void testIsValidObjectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setObjectId(OBJECT_ID);
    assertTrue(LinkUtils.isValid(test));
  }

  /**
   * Tests the result when the link contains a non-empty external id bundle.
   */
  @Test
  public void testIsValidExternalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertTrue(LinkUtils.isValid(test));
  }

  /**
   * Tests the result when the link contains both id types.
   */
  @Test
  public void testIdValidBothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    test.setObjectId(OBJECT_ID);
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    assertTrue(LinkUtils.isValid(test));
  }

  /**
   * Tests the result when the link is null.
   */
  @Test
  public void testIsValidNull() {
    assertFalse(LinkUtils.isValid(null));
  }

  /**
   * Tests the result when the id bundle is empty.
   */
  @Test
  public void testIsValidEmptyBundle() {
    final SimpleSecurityLink test = new SimpleSecurityLink(ExternalIdBundle.EMPTY);
    assertFalse(LinkUtils.isValid(test));
  }
}
