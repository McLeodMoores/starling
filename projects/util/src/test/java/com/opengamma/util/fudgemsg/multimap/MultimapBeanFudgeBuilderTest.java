/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.multimap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the Multimap support in joda-beans/fudge.
 */
@Test(groups = TestGroup.UNIT)
public class MultimapBeanFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  /**
   *
   */
  public void testEmptySimpleMultimap() {
    final SimpleMultimapMockBean bean = SimpleMultimapMockBean.builder().simpleMultimap(ArrayListMultimap.<String, String> create()).build();
    assertThat(cycleObject(SimpleMultimapMockBean.class, bean), is(bean));
  }

  /**
   *
   */
  public void testNonEmptySimpleMultimap() {
    final Multimap<String, String> mmap = HashMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");

    final SimpleMultimapMockBean bean = SimpleMultimapMockBean.builder().simpleMultimap(mmap).build();
    assertThat(cycleObject(SimpleMultimapMockBean.class, bean), is(bean));
  }

  /**
   *
   */
  public void testEmptyListMultimap() {
    final ListMultimapMockBean bean = ListMultimapMockBean.builder().listMultimap(ArrayListMultimap.<String, String> create()).build();
    assertThat(cycleObject(ListMultimapMockBean.class, bean), is(bean));
  }

  /**
   *
   */
  public void testNonEmptyListMultimap() {
    final ListMultimap<String, String> mmap = ArrayListMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");

    final ListMultimapMockBean bean = ListMultimapMockBean.builder().listMultimap(mmap).build();
    assertThat(cycleObject(ListMultimapMockBean.class, bean), is(bean));
  }

  /**
   * This test is unreliable at the moment and so disabled. This is due to the way joda-beans treats a property of Multimap. Joda-beans creates a HashMultiMap,
   * but this gets copied by ImmutableMultimap.copyOf() which uses a ListMultimap which depending on JVM means that the created multimaps don't compare as
   * equals. If https://github.com/JodaOrg/joda-beans/issues/64 gets fixed, it should be possible to enable this test.
   */
  @Test(enabled = false)
  public void testCombinedMultimap() {
    final ListMultimap<String, String> lmmap = ArrayListMultimap.create();
    lmmap.put("one", "1");
    lmmap.put("one", "42");
    lmmap.put("two", "2");

    final HashMultimap<String, String> hmmap = HashMultimap.create();
    hmmap.put("three", "100");
    hmmap.put("three", "4200");
    hmmap.put("four", "200");

    final HashMultimap<String, String> mmap = HashMultimap.create();
    mmap.put("seven", "-13");
    mmap.put("seven", "-423");
    mmap.put("eight", "-24");

    final CombinedMultimapMockBean bean = CombinedMultimapMockBean.builder().listMultimap(lmmap).setMultimap(hmmap).noTypeMultimap(mmap).build();
    assertThat(cycleObject(CombinedMultimapMockBean.class, bean), is(bean));
  }

}
