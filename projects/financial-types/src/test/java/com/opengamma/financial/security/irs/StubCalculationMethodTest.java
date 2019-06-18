/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.irs;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link StubCalculationMethod}.
 */
@Test(groups = TestGroup.UNIT)
public class StubCalculationMethodTest extends AbstractBeanTestCase {
  private static final LocalDate FIRST_STUB_END = LocalDate.of(2020, 1, 1);
  private static final LocalDate LAST_STUB_END = LocalDate.of(2022, 1, 1);
  private static final ExternalId REFERENCE_ID = ExternalId.of("eid", "1");

  /**
   * Tests the first stub rate logic.
   */
  public void testHasFirstStubRate() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasFirstStubRate());
    builder.firstStubRate(Double.NaN);
    assertFalse(builder.build().hasFirstStubRate());
    builder.firstStubRate(0.001);
    assertTrue(builder.build().hasFirstStubRate());
  }

  /**
   * Tests the last stub rate logic.
   */
  public void testHasLastStubRate() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasLastStubRate());
    builder.lastStubRate(Double.NaN);
    assertFalse(builder.build().hasLastStubRate());
    builder.lastStubRate(0.001);
    assertTrue(builder.build().hasLastStubRate());
  }

  /**
   * Tests the first stub start reference id logic.
   */
  public void testHashFirstStubStartReferenceRateId() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasFirstStubStartReferenceRateId());
    builder.firstStubStartReferenceRateId(ExternalId.of("eid", "1"));
    assertTrue(builder.build().hasFirstStubStartReferenceRateId());
  }

  /**
   * Tests the first stub end reference id logic.
   */
  public void testHashFirstStubEndReferenceRateId() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasFirstStubEndReferenceRateId());
    builder.firstStubEndReferenceRateId(ExternalId.of("eid", "1"));
    assertTrue(builder.build().hasFirstStubEndReferenceRateId());
  }

  /**
   * Tests the last stub start reference id logic.
   */
  public void testHashLastStubStartReferenceRateId() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasLastStubStartReferenceRateId());
    builder.lastStubStartReferenceRateId(ExternalId.of("eid", "1"));
    assertTrue(builder.build().hasLastStubStartReferenceRateId());
  }

  /**
   * Tests the last stub end reference id logic.
   */
  public void testHashLastStubEndReferenceRateId() {
    final StubCalculationMethod.Builder builder = StubCalculationMethod.builder().type(StubType.LONG_START);
    assertFalse(builder.build().hasLastStubEndReferenceRateId());
    builder.lastStubEndReferenceRateId(ExternalId.of("eid", "1"));
    assertTrue(builder.build().hasLastStubEndReferenceRateId());
  }

  /**
   * Tests that a first stub end date is needed for BOTH.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoFirstStubEndDateForBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).firstStubEndDate(FIRST_STUB_END).build().validate();
  }

  /**
   * Tests that a first stub end date is needed for BOTH.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoLastStubEndDateForBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).lastStubEndDate(LAST_STUB_END).build().validate();
  }

  /**
   * Tests that if the first start reference id is set, the end reference id is
   * needed for BOTH.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstEndRefIdWithStartRefIdBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .firstStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the first end reference id is set, the start reference id is
   * needed for BOTH.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstStartRefIdWithEndRefIdBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .firstStubEndReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last stub start reference id is set, the end reference id
   * is needed for BOTH.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastEndRefIdWithStartRefIdBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .lastStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last end reference id is set, the start reference id is
   * needed for BOTH.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastStartRefIdWithEndRefIdBoth() {
    StubCalculationMethod.builder().type(StubType.BOTH).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END).lastStubEndReferenceRateId(REFERENCE_ID)
    .build().validate();
  }

  /**
   * Tests that if the first start reference id is set, the end reference id is
   * needed for SHORT_START.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstEndRefIdWithStartRefIdShortStart() {
    StubCalculationMethod.builder().type(StubType.SHORT_START).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .firstStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the first end reference id is set, the start reference id is
   * needed for SHORT_START.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstStartRefIdWithEndRefIdShortStart() {
    StubCalculationMethod.builder().type(StubType.SHORT_START).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .firstStubEndReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last stub start reference id is set, the end reference id
   * is needed for SHORT_END.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastEndRefIdWithStartRefIdShortEnd() {
    StubCalculationMethod.builder().type(StubType.SHORT_END).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .lastStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last end reference id is set, the start reference id is
   * needed for SHORT_END.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastStartRefIdWithEndRefIdShortEnd() {
    StubCalculationMethod.builder().type(StubType.SHORT_END).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
    .lastStubEndReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the first start reference id is set, the end reference id is
   * needed for LONG_START.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstEndRefIdWithStartRefIdLongStart() {
    StubCalculationMethod.builder().type(StubType.LONG_START).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
        .firstStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the first end reference id is set, the start reference id is
   * needed for LONG_START.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFirstStartRefIdWithEndRefIdLongStart() {
    StubCalculationMethod.builder().type(StubType.LONG_START).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
        .firstStubEndReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last stub start reference id is set, the end reference id
   * is needed for LONG_END.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastEndRefIdWithStartRefIdLongEnd() {
    StubCalculationMethod.builder().type(StubType.LONG_END).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
        .lastStubStartReferenceRateId(REFERENCE_ID).build().validate();
  }

  /**
   * Tests that if the last end reference id is set, the start reference id is
   * needed for LONG_END.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoLastStartRefIdWithEndRefIdLongEnd() {
    StubCalculationMethod.builder().type(StubType.LONG_END).firstStubEndDate(FIRST_STUB_END).lastStubEndDate(LAST_STUB_END)
        .lastStubEndReferenceRateId(REFERENCE_ID).build().validate();
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(StubCalculationMethod.class,
        Arrays.asList("type", "firstStubRate", "lastStubRate", "firstStubEndDate", "lastStubEndDate", "firstStubStartReferenceRateId",
            "firstStubEndReferenceRateId", "lastStubStartReferenceRateId", "lastStubEndReferenceRateId"),
        Arrays.asList(StubType.LONG_END, 0.01, 0.02, LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1), ExternalId.of("eid", "1"), ExternalId.of("eid", "2"),
            ExternalId.of("eid", "3"), ExternalId.of("eid", "4")),
        Arrays.asList(StubType.SHORT_END, 0.015, 0.025, LocalDate.of(2020, 1, 10), LocalDate.of(2022, 1, 10), ExternalId.of("eid", "10"),
            ExternalId.of("eid", "20"), ExternalId.of("eid", "30"), ExternalId.of("eid", "40")));
  }
}
