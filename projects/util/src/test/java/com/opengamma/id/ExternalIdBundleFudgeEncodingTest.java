/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for {@link ExternalIdBundle}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void test() {
    final ExternalIdBundle object = ExternalIdBundle.of(
        ExternalId.of("id1", "value1"),
        ExternalId.of("id2", "value2"));
    assertEncodeDecodeCycle(ExternalIdBundle.class, object);
  }

  /**
   * Tests conversion to a Fudge message.
   */
  @Test
  public void testToFudgeMsg() {
    final ExternalIdBundle sample = ExternalIdBundle.of("A", "B");
    assertNull(ExternalIdBundleFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ExternalIdBundleFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  /**
   * Tests conversion from a null Fudge message.
   */
  @Test
  public void testFromFudgeMsg() {
    assertNull(ExternalIdBundleFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
