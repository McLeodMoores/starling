/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.beancompare;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class BeanCompareTest {

  private static final String UNIQUE_ID = "uniqueId";
  private static final String EXTERNAL_ID_BUNDLE = "externalIdBundle";
  private static final String NAME = "name";

  @Test
  @SuppressWarnings("deprecation")
  public void equalIgnoring() {
    final UniqueId uid1 = UniqueId.of("uid", "123");
    final UniqueId uid2 = UniqueId.of("uid", "124");
    final ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final Bean bean1 = createBean(uid1, eid1, "name1");
    final Bean bean2 = createBean(uid2, eid2, "name1");
    assertFalse(BeanCompare.equalIgnoring(bean1, bean2));
    assertTrue(BeanCompare.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty(UNIQUE_ID)));
  }

  @Test
  public void propertyComparators_same() {
    final UniqueId uid1 = UniqueId.of("uid", "123");
    final UniqueId uid2 = UniqueId.of("uid", "123");
    final ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final Bean bean1 = createBean(uid1, eid1, "name1");
    final Bean bean2 = createBean(uid2, eid2, "name1");
    final BeanCompare beanCompare = new BeanCompare();
    final List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertTrue(diff.isEmpty());
  }

  @Test
  public void propertyComparators_different() {
    final UniqueId uid1 = UniqueId.of("uid", "123");
    final UniqueId uid2 = UniqueId.of("uid", "123");
    final ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid2", "abc"));
    final Bean bean1 = createBean(uid1, eid1, "name1");
    final Bean bean2 = createBean(uid2, eid2, "name1");
    final BeanCompare beanCompare = new BeanCompare();
    final List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertFalse(diff.isEmpty());
  }

  @Test
  public void propertyComparators_ignoreDifferences() {
    final Comparator<Object> alwaysEqualComparator = new Comparator<Object>() {
      @Override
      public int compare(final Object notUsed1, final Object notUsed2) {
        return 0;
      }
    };
    final UniqueId uid1 = UniqueId.of("uid", "123");
    final UniqueId uid2 = UniqueId.of("uid", "321");
    final ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    final ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid2", "abc"));
    final Bean bean1 = createBean(uid1, eid1, "name1");
    final Bean bean2 = createBean(uid2, eid2, "name1");
    final MetaProperty<Object> uniqueIdMeta = bean1.property(UNIQUE_ID).metaProperty();
    final MetaProperty<Object> externalIdMeta = bean1.property(EXTERNAL_ID_BUNDLE).metaProperty();
    final Map<MetaProperty<?>, Comparator<Object>> comparators =
        ImmutableMap.<MetaProperty<?>, Comparator<Object>>of(
            uniqueIdMeta, alwaysEqualComparator,
            externalIdMeta, alwaysEqualComparator);
    final BeanCompare beanCompare = new BeanCompare(comparators, Collections.<Class<?>, Comparator<Object>>emptyMap());
    // same despite different IDs
    final List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertTrue(diff.toString(), diff.isEmpty());
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
