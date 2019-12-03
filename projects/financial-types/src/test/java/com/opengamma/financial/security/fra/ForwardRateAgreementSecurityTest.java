/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.fra;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ForwardRateAgreementSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementSecurityTest extends AbstractBeanTestCase {
  private static final ExternalId UNDERLYING_ID = ExternalId.of("index", "3M IBOR");
  private static final Currency CCY = Currency.AUD;
  private static final Frequency FREQUENCY = SimpleFrequency.QUARTERLY;
  private static final LocalDate START_DATE = LocalDate.of(2020, 1, 1);
  private static final LocalDate END_DATE = LocalDate.of(2020, 4, 1);
  private static final double RATE = 0.001;
  private static final double AMOUNT = 1000000;
  private static final LocalDate FIXING_DATE = LocalDate.of(2020, 1, 3);
  private static final DayCount DC = DayCounts.ACT_360;
  private static final BusinessDayConvention FIXING_BDC = BusinessDayConventions.FOLLOWING;
  private static final Set<ExternalId> FIXING_CALENDARS = Collections.singleton(ExternalId.of("bank hol", "au"));
  private static final Set<ExternalId> PAYMENT_CALENDARS = Collections.singleton(ExternalId.of("hol", "au"));
  private static final Integer FIXING_LAG = 2;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(ForwardRateAgreementSecurity.class,
        Arrays.asList("securityType", "currency", "underlyingId", "indexFrequency", "startDate", "endDate", "rate", "amount", "fixingDate", "dayCount",
            "fixingBusinessDayConvention", "calendars", "paymentCalendars", "fixingLag"),
        Arrays.asList(ForwardRateAgreementSecurity.SECURITY_TYPE, CCY, UNDERLYING_ID, FREQUENCY, START_DATE, END_DATE, RATE, AMOUNT, FIXING_DATE, DC,
            FIXING_BDC, FIXING_CALENDARS, PAYMENT_CALENDARS, FIXING_LAG),
        Arrays.asList(FRASecurity.SECURITY_TYPE, Currency.BRL, ExternalId.of("index", "6M IBOR"), SimpleFrequency.SEMI_ANNUAL, END_DATE, START_DATE, AMOUNT,
            RATE, START_DATE, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, PAYMENT_CALENDARS, FIXING_CALENDARS, FIXING_LAG + 1));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    ForwardRateAgreementSecurity fra = new ForwardRateAgreementSecurity();
    assertEquals(fra.getSecurityType(), ForwardRateAgreementSecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), 0.);
    assertNull(fra.getCurrency());
    assertNull(fra.getCalendars());
    assertNull(fra.getDayCount());
    assertNull(fra.getEndDate());
    assertNull(fra.getFixingBusinessDayConvention());
    assertNull(fra.getFixingDate());
    assertNull(fra.getFixingLag());
    assertNull(fra.getIndexFrequency());
    assertNull(fra.getPaymentCalendars());
    assertEquals(fra.getRate(), 0.);
    assertNull(fra.getStartDate());
    assertNull(fra.getUnderlyingId());
    fra = new ForwardRateAgreementSecurity(CCY, UNDERLYING_ID, FREQUENCY, START_DATE, END_DATE, RATE, AMOUNT, FIXING_DATE, DC, FIXING_BDC, FIXING_CALENDARS,
        FIXING_LAG);
    assertEquals(fra.getSecurityType(), ForwardRateAgreementSecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), AMOUNT);
    assertEquals(fra.getCurrency(), CCY);
    assertEquals(fra.getCalendars(), FIXING_CALENDARS);
    assertEquals(fra.getDayCount(), DC);
    assertEquals(fra.getEndDate(), END_DATE);
    assertEquals(fra.getFixingBusinessDayConvention(), FIXING_BDC);
    assertEquals(fra.getFixingDate(), FIXING_DATE);
    assertEquals(fra.getFixingLag(), FIXING_LAG);
    assertEquals(fra.getIndexFrequency(), FREQUENCY);
    assertEquals(fra.getPaymentCalendars(), FIXING_CALENDARS);
    assertEquals(fra.getRate(), RATE);
    assertEquals(fra.getStartDate(), START_DATE);
    assertEquals(fra.getUnderlyingId(), UNDERLYING_ID);
    fra = new ForwardRateAgreementSecurity(CCY, UNDERLYING_ID, FREQUENCY, START_DATE, END_DATE, RATE, AMOUNT, FIXING_DATE, DC, FIXING_BDC, FIXING_CALENDARS,
        null, FIXING_LAG);
    assertEquals(fra.getSecurityType(), ForwardRateAgreementSecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), AMOUNT);
    assertEquals(fra.getCurrency(), CCY);
    assertEquals(fra.getCalendars(), FIXING_CALENDARS);
    assertEquals(fra.getDayCount(), DC);
    assertEquals(fra.getEndDate(), END_DATE);
    assertEquals(fra.getFixingBusinessDayConvention(), FIXING_BDC);
    assertEquals(fra.getFixingDate(), FIXING_DATE);
    assertEquals(fra.getFixingLag(), FIXING_LAG);
    assertEquals(fra.getIndexFrequency(), FREQUENCY);
    assertEquals(fra.getPaymentCalendars(), FIXING_CALENDARS);
    assertEquals(fra.getRate(), RATE);
    assertEquals(fra.getStartDate(), START_DATE);
    assertEquals(fra.getUnderlyingId(), UNDERLYING_ID);
    fra = new ForwardRateAgreementSecurity(CCY, UNDERLYING_ID, FREQUENCY, START_DATE, END_DATE, RATE, AMOUNT, FIXING_DATE, DC, FIXING_BDC, FIXING_CALENDARS,
        PAYMENT_CALENDARS, FIXING_LAG);
    assertEquals(fra.getSecurityType(), ForwardRateAgreementSecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), AMOUNT);
    assertEquals(fra.getCurrency(), CCY);
    assertEquals(fra.getCalendars(), FIXING_CALENDARS);
    assertEquals(fra.getDayCount(), DC);
    assertEquals(fra.getEndDate(), END_DATE);
    assertEquals(fra.getFixingBusinessDayConvention(), FIXING_BDC);
    assertEquals(fra.getFixingDate(), FIXING_DATE);
    assertEquals(fra.getFixingLag(), FIXING_LAG);
    assertEquals(fra.getIndexFrequency(), FREQUENCY);
    assertEquals(fra.getPaymentCalendars(), PAYMENT_CALENDARS);
    assertEquals(fra.getRate(), RATE);
    assertEquals(fra.getStartDate(), START_DATE);
    assertEquals(fra.getUnderlyingId(), UNDERLYING_ID);
  }

  /**
   * Tests that accept() calls the correct visitor method.
   */
  public void testAccept() {
    final ForwardRateAgreementSecurity fra = new ForwardRateAgreementSecurity(CCY, UNDERLYING_ID, FREQUENCY, START_DATE, END_DATE, RATE, AMOUNT, FIXING_DATE,
        DC, FIXING_BDC, FIXING_CALENDARS, PAYMENT_CALENDARS, FIXING_LAG);
    assertEquals(fra.accept(TestVisitor.INSTANCE), "ForwardRateAgreementSecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
