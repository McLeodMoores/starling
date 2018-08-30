/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

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
 * Test Fudge encoding of {@link ExternalId}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests the encoding and decoding of an ExternalId.
   */
  @Test
  public void testBuilder() {
    final FudgeContext context = new FudgeContext();
    context.getObjectDictionary().addBuilder(ExternalId.class, new ExternalIdFudgeBuilder());
    setContext(context);
    final ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

  /**
   * Tests the encoding and deciding of an ExternalId using the secondary type.
   */
  @Test
  public void testSecondaryType() {
    final FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(ExternalIdFudgeSecondaryType.INSTANCE);
    setContext(context);
    final ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

  /**
   * Tests that a null id returns null and a non-null id returns a message.
   */
  @Test
  public void testToFudgeMsg() {
    final ExternalId sample = ExternalId.of("A", "B");
    assertNull(ExternalIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ExternalIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  /**
   * Tests that a null message returns null.
   */
  @Test
  public void testFromFudgeMsg() {
    assertNull(ExternalIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
    final ExternalId id = ExternalId.of("A", "N");
    final FudgeMsg msg = ExternalIdFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), id);
    assertNotNull(ExternalIdFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg));
  }

}
