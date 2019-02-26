/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.swap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link BondTotalReturnSwapSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class BondTotalReturnSwapSecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.AUD;
  private static final double NOTIONAL = 100000;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Frequency FREQ = SimpleFrequency.SEMI_ANNUAL;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final ExternalId FLOATING_ID = ExternalId.of("eid", "1");
  private static final ExternalIdBundle ASSET_ID = ExternalIdBundle.of("eid", "2");
  private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2020, 1, 5);
  private static final LocalDate MATURITY_DATE = LocalDate.of(2022, 1, 15);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Set<ExternalId> CALENDARS = Collections.singleton(ExternalId.of("eid", "3"));
  private static final DateRelativeTo RESET = DateRelativeTo.START;
  private static final RollConvention ROLL = RollConvention.FIFTEEN;
  private static final int[] DATES = { 0, 4 };
  private static final LocalDate[] PAYMENT_DATES = { LocalDate.of(2020, 1, 13), LocalDate.of(2022, 1, 18) };
  private static final FloatingInterestRateSwapLeg FLOATING = new FloatingInterestRateSwapLeg();
  static {
    FLOATING.setDayCountConvention(DC);
    FLOATING.setFloatingReferenceRateId(ExternalId.of("eid", "4"));
    FLOATING.setFloatingRateType(FloatingRateType.IBOR);
    FLOATING.setResetPeriodBusinessDayConvention(BDC);
    FLOATING.setPayReceiveType(PayReceiveType.PAY);
    FLOATING.setMaturityDateBusinessDayConvention(BDC);
    FLOATING.setPaymentDateBusinessDayConvention(BDC);
    FLOATING.setPaymentDateFrequency(FREQ);
    FLOATING.setAccrualPeriodBusinessDayConvention(BDC);
    FLOATING.setAccrualPeriodFrequency(FREQ);
    FLOATING.setResetPeriodFrequency(FREQ);
    FLOATING.setFixingDateBusinessDayConvention(BDC);
    FLOATING.setNotional(new InterestRateSwapNotional(Currency.USD, 1000000));
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final FloatingInterestRateSwapLeg otherFloating = FLOATING.clone();
    otherFloating.setFloatingReferenceRateId(ExternalId.of("eid", "5"));
    return new JodaBeanProperties<>(BondTotalReturnSwapSecurity.class,
        Arrays.asList("notionalCurrency", "notionalAmount", "fundingLeg", "assetId", "effectiveDate", "maturityDate", "paymentSettlementDays",
            "paymentBusinessDayConvention", "paymentFrequency", "rollConvention", "paymentDateCalendar", "resetDateRelativeTo", "dates", "paymentDates"),
        Arrays.asList(CCY, NOTIONAL, FLOATING, ASSET_ID, EFFECTIVE_DATE, MATURITY_DATE, SETTLEMENT_DAYS, BDC, FREQ, ROLL, CALENDARS, RESET, DATES,
            PAYMENT_DATES),
        Arrays.asList(Currency.BRL, NOTIONAL * 2, otherFloating, FLOATING_ID.toBundle(), MATURITY_DATE, EFFECTIVE_DATE, SETTLEMENT_DAYS + 1,
            BusinessDayConventions.MODIFIED_FOLLOWING, SimpleFrequency.ANNUAL, RollConvention.EIGHT, Collections.<ExternalId> emptySet(), DateRelativeTo.END,
            new int[0], new LocalDate[0]));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    BondTotalReturnSwapSecurity security = new BondTotalReturnSwapSecurity();
    assertEquals(security.getSecurityType(), BondTotalReturnSwapSecurity.SECURITY_TYPE);
    assertNull(security.getFundingLeg());
    assertNull(security.getAssetId());
    assertNull(security.getEffectiveDate());
    assertNull(security.getMaturityDate());
    assertNull(security.getNotionalCurrency());
    assertNull(security.getNotionalAmount());
    assertEquals(security.getPaymentSettlementDays(), 0);
    assertNull(security.getPaymentBusinessDayConvention());
    assertNull(security.getPaymentFrequency());
    assertNull(security.getRollConvention());
    security = new BondTotalReturnSwapSecurity(FLOATING, ASSET_ID, EFFECTIVE_DATE, MATURITY_DATE, CCY, NOTIONAL, SETTLEMENT_DAYS, BDC, FREQ, ROLL);
    assertEquals(security.getSecurityType(), BondTotalReturnSwapSecurity.SECURITY_TYPE);
    assertEquals(security.getFundingLeg(), FLOATING);
    assertEquals(security.getAssetId(), ASSET_ID);
    assertEquals(security.getEffectiveDate(), EFFECTIVE_DATE);
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getNotionalCurrency(), CCY);
    assertEquals(security.getNotionalAmount(), NOTIONAL);
    assertEquals(security.getPaymentSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(security.getPaymentBusinessDayConvention(), BDC);
    assertEquals(security.getPaymentFrequency(), FREQ);
    assertEquals(security.getRollConvention(), ROLL);
  }

  /**
   * Tests that the accept method points to the right method in the visitor.
   */
  public void testAccept() {
    final BondTotalReturnSwapSecurity security = new BondTotalReturnSwapSecurity(FLOATING, ASSET_ID, EFFECTIVE_DATE, MATURITY_DATE, CCY, NOTIONAL,
        SETTLEMENT_DAYS, BDC, FREQ, ROLL);
    assertEquals(security.accept(TestVisitor.INSTANCE).intValue(), SETTLEMENT_DAYS);
  }

  /**
   * Tests the payment dates when no overrides are provided.
   */
  public void testPaymentDatesUseConvention() {
    final BondTotalReturnSwapSecurity security = new BondTotalReturnSwapSecurity(FLOATING, ASSET_ID, EFFECTIVE_DATE, MATURITY_DATE, CCY, NOTIONAL,
        SETTLEMENT_DAYS, BDC, FREQ, ROLL);
    final List<LocalDate> payments = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      payments.add(security.getPaymentDate(i, EFFECTIVE_DATE, WeekendWorkingDayCalendar.SATURDAY_SUNDAY));
    }
    assertEquals(payments,
        Arrays.asList(LocalDate.of(2020, 1, 15), LocalDate.of(2020, 7, 15), LocalDate.of(2021, 1, 15), LocalDate.of(2021, 7, 15), LocalDate.of(2022, 1, 17)));
    security.setDates(new int[0]);
    security.setPaymentDates(new LocalDate[0]);
    payments.clear();
    for (int i = 0; i < 5; i++) {
      payments.add(security.getPaymentDate(i, EFFECTIVE_DATE, WeekendWorkingDayCalendar.SATURDAY_SUNDAY));
    }
    assertEquals(payments,
        Arrays.asList(LocalDate.of(2020, 1, 15), LocalDate.of(2020, 7, 15), LocalDate.of(2021, 1, 15), LocalDate.of(2021, 7, 15), LocalDate.of(2022, 1, 17)));
  }

  /**
   * Tests the payment dates when overrides are provided.
   */
  public void testPaymentDatesUseOverrides() {
    final BondTotalReturnSwapSecurity security = new BondTotalReturnSwapSecurity(FLOATING, ASSET_ID, EFFECTIVE_DATE, MATURITY_DATE, CCY, NOTIONAL,
        SETTLEMENT_DAYS, BDC, FREQ, ROLL);
    security.setDates(DATES);
    security.setPaymentDates(PAYMENT_DATES);
    final List<LocalDate> payments = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      payments.add(security.getPaymentDate(i, EFFECTIVE_DATE, WeekendWorkingDayCalendar.SATURDAY_SUNDAY));
    }
    assertEquals(payments,
        Arrays.asList(LocalDate.of(2020, 1, 13), LocalDate.of(2020, 7, 15), LocalDate.of(2021, 1, 15), LocalDate.of(2021, 7, 15), LocalDate.of(2022, 1, 18)));
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<Integer> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public Integer visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
      return security.getPaymentSettlementDays();
    }
  }
}
