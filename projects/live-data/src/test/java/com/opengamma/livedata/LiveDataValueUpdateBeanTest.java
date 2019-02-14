/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LiveDataValueUpdateBean} and its Fudge builder.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataValueUpdateBeanTest extends AbstractFudgeBuilderTestCase {
  private static final long SEQUENCE_NUMBER = 2;
  private static final LiveDataSpecification SPEC = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2")));
  private static final MutableFudgeMsg FIELD_CONTAINER = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();

  /**
   * Tests that the sequence number cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSequenceNumber() {
    new LiveDataValueUpdateBean(-SEQUENCE_NUMBER, SPEC, FIELD_CONTAINER);
  }

  /**
   * Tests that the specification cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpecification() {
    new LiveDataValueUpdateBean(SEQUENCE_NUMBER, null, FIELD_CONTAINER);
  }

  /**
   * Tests that the field container cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFieldContainer() {
    new LiveDataValueUpdateBean(SEQUENCE_NUMBER, SPEC, null);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LiveDataValueUpdateBean bean = new LiveDataValueUpdateBean(SEQUENCE_NUMBER, SPEC, FIELD_CONTAINER);
    assertEquals(bean.getFields(), FIELD_CONTAINER);
    assertEquals(bean.getSequenceNumber(), SEQUENCE_NUMBER);
    assertEquals(bean.getSpecification(), SPEC);
    assertNotEquals(SPEC, bean);
    assertEquals(bean, bean);
    assertEquals(bean.toString(), "LiveDataValueUpdateBean[2, LiveDataSpecification[Bundle[eid~1, eid~2]:rules], FudgeMsg[]]");
    LiveDataValueUpdateBean other = new LiveDataValueUpdateBean(SEQUENCE_NUMBER, SPEC, FIELD_CONTAINER);
    assertEquals(bean, other);
    assertEquals(bean.hashCode(), other.hashCode());
    other = new LiveDataValueUpdateBean(SEQUENCE_NUMBER + 1, SPEC, FIELD_CONTAINER);
    assertNotEquals(bean, other);
    other = new LiveDataValueUpdateBean(SEQUENCE_NUMBER, new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"))), FIELD_CONTAINER);
    assertNotEquals(bean, other);
    final MutableFudgeMsg msg = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();
    msg.add("field", "value");
    other = new LiveDataValueUpdateBean(SEQUENCE_NUMBER, SPEC, msg);
    assertNotEquals(bean, other);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LiveDataValueUpdateBean bean = new LiveDataValueUpdateBean(SEQUENCE_NUMBER, SPEC, FIELD_CONTAINER);
    assertEncodeDecodeCycle(LiveDataValueUpdateBean.class, bean);
  }
}
