/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.testutils;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Base class for testing Fudge serialization and deserialization.
 */
public class FudgeTestBase {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FudgeTestBase.class);
  /** The Fudge context used in tests */
  private FudgeContext _fudgeContext;

  /**
   * Creates the Fudge context.
   */
  @BeforeMethod(groups = TestGroup.UNIT)
  public void createFudgeContext() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
  }

  /**
   * Gets the Fudge context.
   * @return  the context
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Cycles a Fudge message to and from a byte array.
   * @param message  the message
   * @return  the cycled message
   */
  private FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] data = _fudgeContext.toByteArray(message);
    LOGGER.info("{} bytes", data.length);
    return _fudgeContext.deserialize(data).getMessage();
  }

  /**
   * Serializes and deserializes an object using Fudge.
   * @param clazz  the object class
   * @param object  the object
   * @return  the cycled object
   */
  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    final T newObject = cycleGenericObject(clazz, object);
    assertEquals(object.getClass(), newObject.getClass());
    return newObject;
  }

  /**
   * Serializes and deserializes an object using Fudge.
   * @param clazz  the object class
   * @param object  the object
   * @return  the cycled object
   */
  protected <T> T cycleGenericObject(final Class<T> clazz, final T object) {
    LOGGER.info("object {}", object);
    final FudgeSerializer fudgeSerializationContext = new FudgeSerializer(_fudgeContext);
    final FudgeDeserializer fudgeDeserializationContext = new FudgeDeserializer(_fudgeContext);
    final MutableFudgeMsg messageIn = fudgeSerializationContext.newMessage();
    fudgeSerializationContext.addToMessageWithClassHeaders(messageIn, "test", null, object, clazz);
    LOGGER.info("message {}", messageIn);
    final FudgeMsg messageOut = cycleMessage(messageIn);
    LOGGER.info("message {}", messageOut);
    final T newObject = fudgeDeserializationContext.fieldValueToObject(clazz, messageOut.getByName("test"));
    assertNotNull(newObject);
    LOGGER.info("object {}", newObject);
    assertTrue(clazz.isAssignableFrom(newObject.getClass()));
    return newObject;
  }

}
