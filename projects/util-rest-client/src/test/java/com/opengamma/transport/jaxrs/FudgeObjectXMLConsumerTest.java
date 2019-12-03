/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;

import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeObjectXMLConsumer}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeObjectXMLConsumerTest {
  private static final FudgeObjectXMLConsumer CONSUMER = new FudgeObjectXMLConsumer();

  /**
   * Tests that a type is treated as a bean.
   */
  @Test
  public void testReadable() {
    assertTrue(CONSUMER.isReadable(FudgeMsgEnvelope.class, Double.TYPE, new Annotation[0], MediaType.TEXT_HTML_TYPE));
    assertFalse(CONSUMER.isReadable(Object.class, Double.TYPE, new Annotation[0], MediaType.TEXT_HTML_TYPE));
    assertFalse(CONSUMER.isReadable(String.class, Double.TYPE, new Annotation[0], MediaType.TEXT_HTML_TYPE));
  }

}
