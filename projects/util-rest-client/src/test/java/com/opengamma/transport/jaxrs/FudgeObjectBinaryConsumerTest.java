/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;

import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeObjectBinaryConsumer}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeObjectBinaryConsumerTest {
  private static final FudgeObjectBinaryConsumer CONSUMER = new FudgeObjectBinaryConsumer(OpenGammaFudgeContext.getInstance());

  /**
   * Tests that a type is treated as a bean.
   */
  @Test
  public void testReadable() {
    assertTrue(CONSUMER.isReadable(Object.class, Double.TYPE, new Annotation[0], MediaType.TEXT_HTML_TYPE));
  }

}
