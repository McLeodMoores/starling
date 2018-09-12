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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for {@link UniqueId}.
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests encoding/decoding of a simple unique id.
   */
  @Test
  public void testSimple() {
    final FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(UniqueId.class, new UniqueIdFudgeBuilder());
    setContext(context);
    final UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  /**
   * Tests encoding/decoding of a versioned unique id.
   */
  @Test
  public void testVersioned() {
    final FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(UniqueId.class, new UniqueIdFudgeBuilder());
    setContext(context);
    final UniqueId object = UniqueId.of("A", "B", "C");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  /**
   * Tests encoding/decoding of a simple unique id using the secondary type.
   */
  @Test
  public void testSecondaryType() {
    final FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(UniqueIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    final UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  /**
   * Tests conversion to a message.
   */
  @Test
  public void testToFudgeMsg() {
    final UniqueId sample = UniqueId.of("A", "B", "C");
    assertNull(UniqueIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(UniqueIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  /**
   * Tests conversion from a null message.
   */
  @Test
  public void testfromFudgeMsgNull() {
    assertNull(UniqueIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

  /**
   * Tests conversion from a message.
   */
  public void testFromFudgeMsg() {
    final UniqueId uid = UniqueId.of("A", "B");
    final FudgeMsg msg = UniqueIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), uid);
    assertEquals(UniqueIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg), uid);
  }

  /**
   * Tests than an empty message cannot be decoded.
   */
  @Test(expectedExceptions = RuntimeException.class)
  public void testFromFudgeMsgEmpty() {
    final FudgeMsg msg = getFudgeContext().newMessage();
    UniqueIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

}
