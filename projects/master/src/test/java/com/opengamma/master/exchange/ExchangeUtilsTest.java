/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.master.exchange.impl.InMemoryExchangeMaster;
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests for {@link ExchangeUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeUtilsTest {
  private static final InMemoryExchangeMaster MASTER = new InMemoryExchangeMaster();
  private static final ExchangeSource SOURCE = new MasterExchangeSource(MASTER);
  private static final ExternalId ID_1 = ExternalSchemes.isoMicExchangeId("ABCD");
  private static final ExternalId ID_2 = ExternalSchemes.isoMicExchangeId("EFGH");
  private static final String PHASE_NAME = "Trading";
  private static final ManageableExchangeDetail DETAIL = new ManageableExchangeDetail();
  private static final LocalDate TODAY = LocalDate.of(2018, 1, 1);
  private static final LocalTime PHASE_END = LocalTime.of(13, 0);
  private static final LocalTime DEFAULT_TIME = LocalTime.of(15, 0);
  private static final ZoneId TIME_ZONE = ZoneId.of("Europe/London");
  private static final Pair<LocalTime, ZoneId> DEFAULT_TRADING_CLOSE_TIME = Pairs.of(DEFAULT_TIME, TIME_ZONE);
  private static final ManageableExchange EXCH = new ManageableExchange();
  static {
    EXCH.setISOMic(ID_1.getValue());
    EXCH.setTimeZone(TIME_ZONE);
    EXCH.setDetail(Collections.singletonList(DETAIL));
    MASTER.add(new ExchangeDocument(EXCH));
  }

  /**
   *
   */
  @AfterMethod
  public void resetDetails() {
    DETAIL.setCalendarEnd(null);
    DETAIL.setCalendarStart(null);
    DETAIL.setDayEnd(null);
    DETAIL.setDayRangeType(null);
    DETAIL.setDayStart(null);
    DETAIL.setLastConfirmed(null);
    DETAIL.setNotes(null);
    DETAIL.setPhaseEnd(null);
    DETAIL.setPhaseName(null);
    DETAIL.setPhaseStart(null);
    DETAIL.setPhaseType(null);
    DETAIL.setProductCode(null);
    DETAIL.setProductGroup(null);
    DETAIL.setProductName(null);
    DETAIL.setProductType(null);
    DETAIL.setRandomEndMax(null);
    DETAIL.setRandomEndMin(null);
    DETAIL.setRandomStartMax(null);
    DETAIL.setRandomStartMin(null);
  }

  /**
   * Tests that the exchange source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTradingCloseTimeNullExchangeSource() {
    ExchangeUtils.getTradingCloseTime(null, ID_1, TODAY, DEFAULT_TIME);
  }

  /**
   * Tests the return when there is no exchange for the id in the source.
   */
  public void testNoExchangeForId() {
    assertNull(ExchangeUtils.getTradingCloseTime(SOURCE, ID_2, TODAY, DEFAULT_TIME));
  }

  /**
   * Tests that the phase name must equal "Trading".
   */
  public void testPhaseNameIsTrading() {
    // note it is case sensitive
    DETAIL.setPhaseName("trading");
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseName("random");
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseName(PHASE_NAME);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
  }

  /**
   * Tests that if the calendar start is today, the phase end is used.
   */
  public void testCalendarStartIsToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarStart(TODAY);
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), Pairs.of(PHASE_END, TIME_ZONE));
  }

  /**
   * Tests that if the calendar start is before today, the phase end is used.
   */
  public void testCalendarStartIsBeforeToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarStart(TODAY.minusDays(100));
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), Pairs.of(PHASE_END, TIME_ZONE));
  }

  /**
   * Tests that if the calendar start is after today, the default end time is used.
   */
  public void testCalendarStartIsAfterToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarStart(TODAY.plusDays(100));
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
  }

  /**
   * Tests that if the calendar end is today, the phase end is used.
   */
  public void testCalendarEndIsToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarEnd(TODAY);
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), Pairs.of(PHASE_END, TIME_ZONE));
  }

  /**
   * Tests that if the calendar end is before today, the default end time is used.
   */
  public void testCalendarEndIsBeforeToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarEnd(TODAY.minusDays(100));
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
  }

  /**
   * Tests that if the calendar end is after today, phase end is used.
   */
  public void testCalendarEndIsAfterToday() {
    DETAIL.setPhaseName(PHASE_NAME);
    DETAIL.setCalendarEnd(TODAY.plusDays(100));
    // default used because phase end isn't set
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), DEFAULT_TRADING_CLOSE_TIME);
    DETAIL.setPhaseEnd(PHASE_END);
    assertEquals(ExchangeUtils.getTradingCloseTime(SOURCE, ID_1, TODAY, DEFAULT_TIME), Pairs.of(PHASE_END, TIME_ZONE));
  }
}
