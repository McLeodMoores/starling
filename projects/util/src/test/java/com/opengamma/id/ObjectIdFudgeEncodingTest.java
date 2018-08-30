/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for a {@link ObjectId}.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests encoding/decoding an object id.
   */
  public void testBuilder() {
    final FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(ObjectId.class, new ObjectIdFudgeBuilder());
    setContext(context);
    final ObjectId object = ObjectId.of("A", "B");
    assertEncodeDecodeCycle(ObjectId.class, object);
  }

  /**
   * Tests encoding/decoding the secondary type of an object id.
   */
  @Test
  public void testSecondaryType() {
    final FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(ObjectIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    final ObjectId object = ObjectId.of("A", "B");
    assertEncodeDecodeCycle(ObjectId.class, object);
  }

  /**
   * Tests serialization of messages.
   */
  @Test
  public void testToFudgeMsg() {
    final ObjectId sample = ObjectId.of("A", "B");
    assertNull(ObjectIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ObjectIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  /**
   * Tests that a null object produces a null output.
   */
  @Test
  public void testFromFudgeMsg() {
    final ObjectId oid = ObjectId.parse("A~B");
    final MutableFudgeMsg msg = ObjectIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), oid);
    assertEquals(ObjectIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg), oid);
  }

  /**
   * Tests that deserializing an empty Fudge message throws an exception.
   */
  @Test(expectedExceptions = RuntimeException.class)
  public void testFromFudgeMsgEmpty() {
    final FudgeMsg msg = getFudgeContext().newMessage();
    ObjectIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that a null message produces a null output.
   */
  @Test
  public void testFromFudgeMsgNull() {
    assertNull(ObjectIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }
}
