/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;


import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalIdBundle;
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

  public void test_best_empty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertEquals(ExternalIdBundle.EMPTY, LinkUtils.best(test));
  }

  public void test_best_objectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    assertEquals(OBJECT_ID, LinkUtils.best(test));
  }

  public void test_best_externalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertEquals(EXTERNAL_ID_BUNDLE, LinkUtils.best(test));
  }

  public void test_best_bothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    assertEquals(OBJECT_ID, LinkUtils.best(test));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_best_null() {
    assertEquals(null, LinkUtils.best(null));
  }

  //-------------------------------------------------------------------------
  public void test_bestName_empty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertEquals("", LinkUtils.bestName(test));
  }

  public void test_bestName_objectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    assertEquals("A~B", LinkUtils.bestName(test));
  }

  public void test_bestName_externalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertEquals("D", LinkUtils.bestName(test));
  }

  public void test_bestName_bothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    assertEquals("D", LinkUtils.bestName(test));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_bestName_null() {
    assertEquals(null, LinkUtils.bestName(null));
  }

  //-------------------------------------------------------------------------
  public void test_isValid_empty() {
    final SimpleSecurityLink test = new SimpleSecurityLink();
    assertEquals(false, LinkUtils.isValid(test));
  }

  public void test_isValid_objectId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    assertEquals(true, LinkUtils.isValid(test));
  }

  public void test_isValid_externalId() {
    final SimpleSecurityLink test = new SimpleSecurityLink(EXTERNAL_ID_BUNDLE);
    assertEquals(true, LinkUtils.isValid(test));
  }

  public void test_isValid_bothIds() {
    final SimpleSecurityLink test = new SimpleSecurityLink(OBJECT_ID);
    test.setExternalId(EXTERNAL_ID_BUNDLE);
    assertEquals(true, LinkUtils.isValid(test));
  }

  public void test_isValid_null() {
    assertEquals(false, LinkUtils.isValid(null));
  }

}
