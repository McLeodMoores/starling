/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.integration.finmath.testutils;

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
 * Base class for testing the conversion of OG-Financial objects to and from Fudge messages.
 */
public class FinancialTestBase {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FinancialTestBase.class);
  /** The fudge context */
  private FudgeContext _fudgeContext;

  /**
   * Creates a fudge context before the tests are run.
   */
  @BeforeMethod(groups = TestGroup.UNIT)
  public void createFudgeContext() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
  }

  /**
   * Gets the fudge context.
   * @return The fudge context
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Converts a fudge message to a byte array and then deseria1lizes.
   * @param message The message
   * @return The cycled message
   */
  private FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] data = getFudgeContext().toByteArray(message);
    LOGGER.info("{} bytes", data.length);
    return getFudgeContext().deserialize(data).getMessage();
  }

  /**
   * Cycles an object using the fudge context and tests that the cycled class
   * is the same type as that expected.
   * @param <T> The type of the object
   * @param clazz The class of the object
   * @param object The object
   * @return The cycled object
   */
  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    final T newObject = cycleGenericObject(clazz, object);
    assertEquals(object.getClass(), newObject.getClass());
    return newObject;
  }

  /**
   * Cycles a generic object using the fudge context and tests that the cycled class
   * is not null and that the expected class is assignable from the cycled class.
   * @param <T> The type of the object
   * @param clazz The class of the object
   * @param object The object
   * @return The cycled object
   */
  protected <T> T cycleGenericObject(final Class<T> clazz, final T object) {
    LOGGER.info("object {}", object);
    final FudgeSerializer fudgeSerializationContext = new FudgeSerializer(getFudgeContext());
    final FudgeDeserializer fudgeDeserializationContext = new FudgeDeserializer(getFudgeContext());
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
