/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for {@link ExternalIdWithDates}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdWithDatesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {
  private static final LocalDate VALID_FROM = LocalDate.of(2010, JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, DECEMBER, 1);

  /**
   * Tests the encoding / decoding cycle for an id with no dates.
   */
  @Test
  public void testNoDates() {
    final ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, null);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  /**
   * Tests the encoding / decoding cycle for an id with valid from and to dates.
   */
  @Test
  public void testWithDates() {
    final ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  /**
   * Tests the encoding / decoding cycle for an id with valid from dates.
   */
  @Test
  public void testWithValidFrom() {
    final ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, null);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  /**
   * Tests the encoding / decoding cycle for an id with valid to dates.
   */
  @Test
  public void testWithValidTo() {
    final ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, VALID_TO);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  /**
   * Tests that a null id returns null and a non-null id returns a message.
   */
  @Test
  public void testToFudgeMsg() {
    final ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    assertNull(ExternalIdWithDatesFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ExternalIdWithDatesFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), id));
  }

  /**
   * Tests that a null message returns null.
   */
  @Test
  public void testFromFudgeMsg() {
    assertNull(ExternalIdWithDatesFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
    final ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    final FudgeMsg msg = ExternalIdWithDatesFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), id);
    assertNotNull(ExternalIdWithDatesFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg));
  }

}
