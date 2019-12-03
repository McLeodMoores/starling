/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.beancompare;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.List;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link BeanDifference}.
 */
@Test(groups = TestGroup.UNIT)
public class BeanDifferenceTest {
  private static final String UNIQUE_ID = "uniqueId";
  private static final String EXTERNAL_ID_BUNDLE = "externalIdBundle";
  private static final String NAME = "name";
  private static final List<BeanDifference<?>> DIFF;
  private static final Bean BEAN_1;
  private static final Bean BEAN_2;

  static {
    final UniqueId uid1 = UniqueId.of("uid", "123");
    final UniqueId uid2 = UniqueId.of("uid", "123");
    final ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid2", "abc"));
    BEAN_1 = createBean(uid1, eid1, "name1");
    BEAN_2 = createBean(uid2, eid2, "name1");
    final BeanCompare beanCompare = new BeanCompare();
    DIFF = beanCompare.compare(BEAN_1, BEAN_2);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final BeanCompare beanCompare = new BeanCompare();
    assertEquals(DIFF, DIFF);
    assertNotEquals(null, DIFF);
    assertNotEquals(BEAN_1, DIFF);
    List<BeanDifference<?>> other = beanCompare.compare(BEAN_1, BEAN_2);
    assertEquals(DIFF, other);
    assertEquals(DIFF.hashCode(), other.hashCode());
    other = beanCompare.compare(BEAN_1, BEAN_1);
    assertNotEquals(DIFF, other);
  }

  /**
   * Tests the getters.
   */
  @SuppressWarnings("unchecked")
  public void testGetters() {
    final BeanDifference<?> diffs = DIFF.get(0);
    assertEquals(diffs.getPath().size(), 1);
    assertEquals(((Set<Object>) diffs.getValue1()).iterator().next(), ExternalId.of("eid1", "321"));
    assertEquals(((Set<Object>) diffs.getValue2()).iterator().next(), ExternalId.of("eid2", "abc"));
  }

  private static Bean createBean(final UniqueId uniqueId, final ExternalIdBundle idBundle, final String name) {
    final FlexiBean bean = new FlexiBean();
    bean.propertyDefine(UNIQUE_ID, UniqueId.class);
    bean.propertyDefine(EXTERNAL_ID_BUNDLE, ExternalIdBundle.class);
    bean.propertyDefine(NAME, String.class);
    bean.propertySet(UNIQUE_ID, uniqueId);
    bean.propertySet(EXTERNAL_ID_BUNDLE, idBundle);
    bean.propertySet(NAME, name);
    return bean;
  }

}
