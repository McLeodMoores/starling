/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import com.opengamma.master.exchange.ManageableExchangeDetail.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableExchangeDetail}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableExchangeDetailTest extends AbstractFudgeBuilderTestCase {
  private static final String PRODUCT_GROUP = "Future Options";
  private static final String PRODUCT_NAME = "Metal Future Option";
  private static final String PRODUCT_TYPE = "Derivatives";
  private static final String PRODUCT_CODE = "AB";
  private static final LocalDate START_DATE = LocalDate.of(2018, 3, 21);
  private static final LocalDate END_DATE = LocalDate.of(2018, 6, 21);
  private static final String DAY_START = "Monday";
  private static final String DAY_RANGE_TYPE = "";
  private static final String DAY_END = "Friday";
  private static final String PHASE_NAME = "Open";
  private static final String PHASE_TYPE = "Trading";
  private static final LocalTime PHASE_START = LocalTime.of(8, 0);
  private static final LocalTime PHASE_END = LocalTime.of(16, 0);
  private static final LocalTime START_MIN = LocalTime.of(7, 59);
  private static final LocalTime START_MAX = LocalTime.of(8, 1);
  private static final LocalTime END_MIN = LocalTime.of(15, 59);
  private static final LocalTime END_MAX = LocalTime.of(16, 5);
  private static final LocalDate LAST_CONFIRMED = LocalDate.of(2017, 12, 31);
  private static final String NOTES = "Nothing";
  private static final ManageableExchangeDetail DETAIL = new ManageableExchangeDetail();
  static {
    DETAIL.setCalendarEnd(END_DATE);
    DETAIL.setCalendarStart(START_DATE);
    DETAIL.setDayEnd(DAY_END);
    DETAIL.setDayRangeType(DAY_RANGE_TYPE);
    DETAIL.setDayStart(DAY_START);
    DETAIL.setLastConfirmed(LAST_CONFIRMED);
    DETAIL.setNotes(NOTES);
    DETAIL.setPhaseEnd(PHASE_END);
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setPhaseStart(PHASE_START);
    DETAIL.setPhaseType(PHASE_TYPE);
    DETAIL.setProductCode(PRODUCT_CODE);
    DETAIL.setProductGroup(PRODUCT_GROUP);
    DETAIL.setProductName(PRODUCT_NAME);
    DETAIL.setProductType(PRODUCT_TYPE);
    DETAIL.setRandomEndMax(END_MAX);
    DETAIL.setRandomEndMin(END_MIN);
    DETAIL.setRandomStartMax(START_MAX);
    DETAIL.setRandomStartMin(START_MIN);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(DETAIL, DETAIL);
    assertNotEquals(null, DETAIL);
    assertEquals(DETAIL.toString(), "ManageableExchangeDetail{productGroup=Future Options, productName=Metal Future Option, "
        + "productType=Derivatives, productCode=AB, calendarStart=2018-03-21, calendarEnd=2018-06-21, dayStart=Monday, "
        + "dayRangeType=, dayEnd=Friday, phaseName=Open, phaseType=Trading, phaseStart=08:00, phaseEnd=16:00, "
        + "randomStartMin=07:59, randomStartMax=08:01, randomEndMin=15:59, randomEndMax=16:05, lastConfirmed=2017-12-31, notes=Nothing}");
    final ManageableExchangeDetail other = new ManageableExchangeDetail();
    other.setCalendarEnd(END_DATE);
    other.setCalendarStart(START_DATE);
    other.setDayEnd(DAY_END);
    other.setDayRangeType(DAY_RANGE_TYPE);
    other.setDayStart(DAY_START);
    other.setLastConfirmed(LAST_CONFIRMED);
    other.setNotes(NOTES);
    other.setPhaseEnd(PHASE_END);
    other.setPhaseName(PHASE_NAME);
    other.setPhaseStart(PHASE_START);
    other.setPhaseType(PHASE_TYPE);
    other.setProductCode(PRODUCT_CODE);
    other.setProductGroup(PRODUCT_GROUP);
    other.setProductName(PRODUCT_NAME);
    other.setProductType(PRODUCT_TYPE);
    other.setRandomEndMax(END_MAX);
    other.setRandomEndMin(END_MIN);
    other.setRandomStartMax(START_MAX);
    other.setRandomStartMin(START_MIN);
    assertEquals(DETAIL, other);
    assertEquals(DETAIL.hashCode(), other.hashCode());
    other.setCalendarEnd(START_DATE);
    assertNotEquals(DETAIL, other);
    other.setCalendarEnd(END_DATE);
    other.setCalendarStart(END_DATE);
    assertNotEquals(DETAIL, other);
    other.setCalendarStart(START_DATE);
    other.setDayEnd(DAY_START);
    assertNotEquals(DETAIL, other);
    other.setDayEnd(DAY_END);
    other.setDayRangeType(NOTES);
    assertNotEquals(DETAIL, other);
    other.setDayRangeType(DAY_RANGE_TYPE);
    other.setDayStart(DAY_END);
    assertNotEquals(DETAIL, other);
    other.setDayStart(DAY_START);
    other.setLastConfirmed(START_DATE);
    assertNotEquals(DETAIL, other);
    other.setLastConfirmed(LAST_CONFIRMED);
    other.setNotes(PRODUCT_GROUP);
    assertNotEquals(DETAIL, other);
    other.setNotes(NOTES);
    other.setPhaseEnd(PHASE_START);
    assertNotEquals(DETAIL, other);
    other.setPhaseEnd(PHASE_END);
    other.setPhaseName(PHASE_TYPE);
    assertNotEquals(DETAIL, other);
    other.setPhaseName(PHASE_NAME);
    other.setPhaseStart(PHASE_END);
    assertNotEquals(DETAIL, other);
    other.setPhaseStart(PHASE_START);
    other.setPhaseType(PRODUCT_TYPE);
    assertNotEquals(DETAIL, other);
    other.setPhaseType(PHASE_TYPE);
    other.setProductCode(PRODUCT_TYPE);
    assertNotEquals(DETAIL, other);
    other.setProductCode(PRODUCT_CODE);
    other.setProductGroup(PRODUCT_NAME);
    assertNotEquals(DETAIL, other);
    other.setProductGroup(PRODUCT_GROUP);
    other.setProductName(PRODUCT_GROUP);
    assertNotEquals(DETAIL, other);
    other.setProductName(PRODUCT_NAME);
    other.setProductType(PRODUCT_NAME);
    assertNotEquals(DETAIL, other);
    other.setProductType(PRODUCT_TYPE);
    other.setRandomEndMax(END_MIN);
    assertNotEquals(DETAIL, other);
    other.setRandomEndMax(END_MAX);
    other.setRandomEndMin(END_MAX);
    assertNotEquals(DETAIL, other);
    other.setRandomEndMin(END_MIN);
    other.setRandomStartMax(START_MIN);
    assertNotEquals(DETAIL, other);
    other.setRandomStartMax(START_MAX);
    other.setRandomStartMin(START_MAX);
    assertNotEquals(DETAIL, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Meta meta = DETAIL.metaBean();
    assertEquals(meta.calendarEnd().get(DETAIL), END_DATE);
    assertEquals(DETAIL.property("calendarEnd").get(), END_DATE);
    assertEquals(meta.calendarStart().get(DETAIL), START_DATE);
    assertEquals(DETAIL.property("calendarStart").get(), START_DATE);
    assertEquals(meta.dayEnd().get(DETAIL), DAY_END);
    assertEquals(DETAIL.property("dayEnd").get(), DAY_END);
    assertEquals(meta.dayRangeType().get(DETAIL), DAY_RANGE_TYPE);
    assertEquals(DETAIL.property("dayRangeType").get(), DAY_RANGE_TYPE);
    assertEquals(meta.dayStart().get(DETAIL), DAY_START);
    assertEquals(DETAIL.property("dayStart").get(), DAY_START);
    assertEquals(meta.lastConfirmed().get(DETAIL), LAST_CONFIRMED);
    assertEquals(DETAIL.property("lastConfirmed").get(), LAST_CONFIRMED);
    assertEquals(meta.notes().get(DETAIL), NOTES);
    assertEquals(DETAIL.property("notes").get(), NOTES);
    assertEquals(meta.phaseEnd().get(DETAIL), PHASE_END);
    assertEquals(DETAIL.property("phaseEnd").get(), PHASE_END);
    assertEquals(meta.phaseName().get(DETAIL), PHASE_NAME);
    assertEquals(DETAIL.property("phaseName").get(), PHASE_NAME);
    assertEquals(meta.phaseStart().get(DETAIL), PHASE_START);
    assertEquals(DETAIL.property("phaseStart").get(), PHASE_START);
    assertEquals(meta.phaseType().get(DETAIL), PHASE_TYPE);
    assertEquals(DETAIL.property("phaseType").get(), PHASE_TYPE);
    assertEquals(meta.productCode().get(DETAIL), PRODUCT_CODE);
    assertEquals(DETAIL.property("productCode").get(), PRODUCT_CODE);
    assertEquals(meta.productGroup().get(DETAIL), PRODUCT_GROUP);
    assertEquals(DETAIL.property("productGroup").get(), PRODUCT_GROUP);
    assertEquals(meta.productName().get(DETAIL), PRODUCT_NAME);
    assertEquals(DETAIL.property("productName").get(), PRODUCT_NAME);
    assertEquals(meta.productType().get(DETAIL), PRODUCT_TYPE);
    assertEquals(DETAIL.property("productType").get(), PRODUCT_TYPE);
    assertEquals(meta.randomEndMax().get(DETAIL), END_MAX);
    assertEquals(DETAIL.property("randomEndMax").get(), END_MAX);
    assertEquals(meta.randomEndMin().get(DETAIL), END_MIN);
    assertEquals(DETAIL.property("randomEndMin").get(), END_MIN);
    assertEquals(meta.randomStartMax().get(DETAIL), START_MAX);
    assertEquals(DETAIL.property("randomStartMax").get(), START_MAX);
    assertEquals(meta.randomStartMin().get(DETAIL), START_MIN);
    assertEquals(DETAIL.property("randomStartMin").get(), START_MIN);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(ManageableExchangeDetail.class, DETAIL);
  }
}
