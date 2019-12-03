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

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for {@link ExternalIdBundleWithDates}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleWithDatesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final LocalDate VALID_FROM = LocalDate.of(2010, JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, DECEMBER, 1);

  /**
   * Tests the encoding/decoding cycle for bundles without dates.
   */
  @Test
  public void testNoDates() {
    final ExternalIdWithDates basic1 = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, null);
    final ExternalIdWithDates basic2 = ExternalIdWithDates.of(ExternalId.of("C", "D"), null, null);
    final ExternalIdBundleWithDates object = ExternalIdBundleWithDates.of(basic1, basic2);
    assertEncodeDecodeCycle(ExternalIdBundleWithDates.class, object);
  }

  /**
   * Tests the encoding/decoding cycle for bundles with dates.
   */
  @Test
  public void testWithDates() {
    final ExternalIdWithDates basic1 = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    final ExternalIdWithDates basic2 = ExternalIdWithDates.of(ExternalId.of("C", "D"), VALID_FROM, VALID_TO);
    final ExternalIdBundleWithDates object = ExternalIdBundleWithDates.of(basic1, basic2);
    assertEncodeDecodeCycle(ExternalIdBundleWithDates.class, object);
  }

  /**
   * Tests conversion to a Fudge message.
   */
  @Test
  public void testToFudgeMsg() {
    final ExternalIdWithDates basic1 = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    final ExternalIdWithDates basic2 = ExternalIdWithDates.of(ExternalId.of("C", "D"), VALID_FROM, VALID_TO);
    final ExternalIdBundleWithDates object = ExternalIdBundleWithDates.of(basic1, basic2);
    assertNull(ExternalIdBundleWithDatesFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(ExternalIdBundleWithDatesFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), object));
  }

  /**
   * Tests conversion from a null Fudge message.
   */
  @Test
  public void testFromFudgeMsg() {
    assertNull(ExternalIdBundleWithDatesFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
