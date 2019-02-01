/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.capfloor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link CapFloorCMSSpreadSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadSecurityTest extends AbstractBeanTestCase {
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2022, 1, 1);
  private static final double NOTIONAL = 100000;
  private static final ExternalId LONG_ID = ExternalId.of("eid", "1");
  private static final ExternalId SHORT_ID = ExternalId.of("eid", "2");
  private static final double STRIKE = 0.001;
  private static final Frequency FREQUENCY = SimpleFrequency.QUARTERLY;
  private static final Currency CCY = Currency.AUD;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final boolean IS_PAYER = true;
  private static final boolean IS_CAP = false;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CapFloorCMSSpreadSecurity.class,
        Arrays.asList("securityType", "startDate", "maturityDate", "notional", "longId", "shortId", "strike", "frequency", "currency", "dayCount", "payer",
            "cap"),
        Arrays.asList(CapFloorCMSSpreadSecurity.SECURITY_TYPE, START_DATE, END_DATE, NOTIONAL, LONG_ID, SHORT_ID, STRIKE, FREQUENCY, CCY, DC, IS_PAYER, IS_CAP),
        Arrays.asList(FRASecurity.SECURITY_TYPE, END_DATE, START_DATE, NOTIONAL * 2, SHORT_ID, LONG_ID, STRIKE * 2, SimpleFrequency.SEMI_ANNUAL, Currency.BRL,
            DayCounts.ACT_365, !IS_PAYER, !IS_CAP));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    CapFloorCMSSpreadSecurity capFloor = new CapFloorCMSSpreadSecurity();
    assertEquals(capFloor.getSecurityType(), CapFloorCMSSpreadSecurity.SECURITY_TYPE);
    assertNull(capFloor.getCurrency());
    assertNull(capFloor.getDayCount());
    assertNull(capFloor.getFrequency());
    assertNull(capFloor.getLongId());
    assertNull(capFloor.getMaturityDate());
    assertEquals(capFloor.getNotional(), 0.);
    assertNull(capFloor.getShortId());
    assertNull(capFloor.getStartDate());
    assertEquals(capFloor.getStrike(), 0.);
    assertFalse(capFloor.isCap());
    assertFalse(capFloor.isPayer());
    capFloor = new CapFloorCMSSpreadSecurity(START_DATE, END_DATE, NOTIONAL, LONG_ID, SHORT_ID, STRIKE, FREQUENCY, CCY, DC, IS_PAYER, IS_CAP);
    assertEquals(capFloor.getSecurityType(), CapFloorCMSSpreadSecurity.SECURITY_TYPE);
    assertEquals(capFloor.getCurrency(), CCY);
    assertEquals(capFloor.getDayCount(), DC);
    assertEquals(capFloor.getFrequency(), FREQUENCY);
    assertEquals(capFloor.getLongId(), LONG_ID);
    assertEquals(capFloor.getMaturityDate(), END_DATE);
    assertEquals(capFloor.getNotional(), NOTIONAL);
    assertEquals(capFloor.getShortId(), SHORT_ID);
    assertEquals(capFloor.getStartDate(), START_DATE);
    assertEquals(capFloor.getStrike(), STRIKE);
    assertEquals(capFloor.isCap(), IS_CAP);
    assertEquals(capFloor.isPayer(), IS_PAYER);
  }

  /**
   * Tests that accept() calls the correct visitor method.
   */
  public void testAccept() {
    final CapFloorCMSSpreadSecurity fra = new CapFloorCMSSpreadSecurity(START_DATE, END_DATE, NOTIONAL, LONG_ID, SHORT_ID, STRIKE, FREQUENCY, CCY, DC, IS_PAYER,
        IS_CAP);
    assertEquals(fra.accept(TestVisitor.INSTANCE), "CapFloorCMSSpreadSecurity");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      return security.getClass().getSimpleName();
    }
  }

}
