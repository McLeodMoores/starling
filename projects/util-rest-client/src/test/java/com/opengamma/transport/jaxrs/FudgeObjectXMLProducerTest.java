/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeObjectXMLProducer}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeObjectXMLProducerTest {
  private static final FudgeObjectJSONProducer PRODUCER = new FudgeObjectJSONProducer();

  /**
   * Tests that the media type must be a Fudge REST type.
   */
  @Test
  public void testMediaType() {
    assertFalse(PRODUCER.isWriteable(Object.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
  }

  /**
   * Tests that the type must be a Fudge REST type.
   */
  @Test
  public void testType() {
    assertFalse(PRODUCER.isWriteable(Object.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
  }

  /**
   * Tests the accepted types.
   */
  @Test
  public void testAcceptedType() {
    assertTrue(PRODUCER.isWriteable(FudgeResponse.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
    assertTrue(PRODUCER.isWriteable(FudgeResponse.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
    assertTrue(PRODUCER.isWriteable(Bean.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
    assertTrue(PRODUCER.isWriteable(FudgeMsgEnvelope.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
    assertTrue(PRODUCER.isWriteable(FudgeMsg.class, Double.TYPE, new Annotation[0], MediaType.APPLICATION_ATOM_XML_TYPE));
    assertTrue(PRODUCER.isWriteable(Object.class, Double.TYPE, new Annotation[0], FudgeRest.MEDIA_TYPE));
  }

  /**
   * Tests the size.
   */
  @Test
  public void testSize() {
    assertEquals(PRODUCER.getSize(new Object(), Object.class, Double.TYPE, new Annotation[0], FudgeRest.MEDIA_TYPE), -1);
  }

}
