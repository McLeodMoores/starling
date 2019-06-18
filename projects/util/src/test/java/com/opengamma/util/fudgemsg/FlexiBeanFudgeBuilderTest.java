/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.Assert.assertEquals;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FlexiBeanFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class FlexiBeanFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  /**
   *
   */
  public void testEmpty() {
    final FlexiBean bean = new FlexiBean();
    assertEquals(cycleObject(FlexiBean.class, bean), bean);
  }

  /**
   *
   */
  public void testSimple() {
    final FlexiBean bean = new FlexiBean();
    bean.put("Foo", "Bar");
    bean.put("Bar", 42d);
    bean.put("Cow", null);
    assertEquals(cycleObject(FlexiBean.class, bean), bean);
  }

  /**
   *
   */
  public void testDeep() {
    final FlexiBean bean = new FlexiBean();
    bean.put("Foo", "Bar");
    bean.put("Bar", 42d);
    final FlexiBean nested = new FlexiBean();
    nested.put("A", "X");
    nested.put("B", "Y");
    bean.put("Cow", nested);
    assertEquals(cycleObject(FlexiBean.class, bean), bean);
  }

}
