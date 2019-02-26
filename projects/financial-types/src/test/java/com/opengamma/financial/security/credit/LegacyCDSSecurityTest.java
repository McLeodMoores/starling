/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.credit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegacyCDSSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class LegacyCDSSecurityTest extends AbstractBeanTestCase {
  private static final LocalDate TRADE_DATE = LocalDate.of(2020, 1, 1);
  private static final LocalDate START_DATE = LocalDate.of(2020, 2, 1);
  private static final LocalDate MATURITY_DATE = LocalDate.of(2022, 1, 1);
  private static final ExternalId REFERENCE_ENTITY = ExternalId.of("eid", "1");
  private static final boolean BUY_PROTECTION = true;
  private static final InterestRateNotional NOTIONAL = new InterestRateNotional(Currency.USD, 10000000);
  private static final DebtSeniority SENIORITY = DebtSeniority.SENIOR;
  private static final double COUPON = 0.01;
  private static final Frequency COUPON_FREQUENCY = SimpleFrequency.ANNUAL;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final Set<ExternalId> CALENDARS = Collections.singleton(ExternalId.of("cal", "1"));
  private static final RestructuringClause RESTRUCTURING_CLAUSE = RestructuringClause.MM;
  private static final InterestRateNotional UPFRONT_PAYMENT = new InterestRateNotional(Currency.USD, 1000);
  private static final LocalDate FEE_SETTLEMENT_DATE = LocalDate.of(2020, 1, 16);
  private static final boolean ACCCRUED_ON_DEFAULT = true;
  private static final double FIXED_RECOVERY = 0.4;
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "2");
  private static final String NAME = "name";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(LegacyCDSSecurity.class,
        Arrays.asList("tradeDate", "startDate", "maturityDate", "referenceEntity", "buyProtection", "notional", "debtSeniority", "coupon", "couponFrequency",
            "dayCount", "businessDayConvention", "calendars", "restructuringClause", "upfrontPayment", "feeSettlementDate", "accruedOnDefault",
            "fixedRecovery"),
        Arrays.asList(TRADE_DATE, START_DATE, MATURITY_DATE, REFERENCE_ENTITY, BUY_PROTECTION, NOTIONAL, SENIORITY, COUPON, COUPON_FREQUENCY, DC, BDC,
            CALENDARS, RESTRUCTURING_CLAUSE, UPFRONT_PAYMENT, FEE_SETTLEMENT_DATE, ACCCRUED_ON_DEFAULT, FIXED_RECOVERY),
        Arrays.asList(MATURITY_DATE, FEE_SETTLEMENT_DATE, TRADE_DATE, ExternalId.of("eid", "2"), !BUY_PROTECTION, UPFRONT_PAYMENT, DebtSeniority.JRSUBUT2,
            COUPON * 5, SimpleFrequency.BIMONTHLY, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            Collections.singleton(ExternalId.of("cal", "2")), RestructuringClause.CR, NOTIONAL, START_DATE, !ACCCRUED_ON_DEFAULT, FIXED_RECOVERY * 2));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    LegacyCDSSecurity security = new LegacyCDSSecurity();
    assertEquals(security.getSecurityType(), LegacyCDSSecurity.SECURITY_TYPE);
    assertNull(security.getBusinessDayConvention());
    assertNull(security.getCalendars());
    assertEquals(security.getCoupon(), 0.);
    assertNull(security.getCouponFrequency());
    assertNull(security.getDayCount());
    assertNull(security.getDebtSeniority());
    assertTrue(security.getExternalIdBundle().isEmpty());
    assertNull(security.getFeeSettlementDate());
    assertNull(security.getFixedRecovery());
    assertNull(security.getMaturityDate());
    assertEquals(security.getName(), "");
    assertNull(security.getNotional());
    assertNull(security.getRestructuringClause());
    assertNull(security.getTradeDate());
    assertNull(security.getUpfrontPayment());
    assertFalse(security.isAccruedOnDefault());
    assertFalse(security.isBuyProtection());
    security = new LegacyCDSSecurity(IDS, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON, SENIORITY, COUPON_FREQUENCY, DC, BDC,
        CALENDARS, RESTRUCTURING_CLAUSE, UPFRONT_PAYMENT, FEE_SETTLEMENT_DATE, ACCCRUED_ON_DEFAULT);
    assertEquals(security.getSecurityType(), LegacyCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getBusinessDayConvention(), BDC);
    assertEquals(security.getCalendars(), CALENDARS);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getCouponFrequency(), COUPON_FREQUENCY);
    assertEquals(security.getDayCount(), DC);
    assertEquals(security.getDebtSeniority(), SENIORITY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getFeeSettlementDate(), FEE_SETTLEMENT_DATE);
    assertNull(security.getFixedRecovery());
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getName(), "");
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getRestructuringClause(), RESTRUCTURING_CLAUSE);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUpfrontPayment(), UPFRONT_PAYMENT);
    assertEquals(security.isAccruedOnDefault(), ACCCRUED_ON_DEFAULT);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
    security = new LegacyCDSSecurity(IDS, NAME, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON, SENIORITY, COUPON_FREQUENCY, DC,
        BDC, CALENDARS, RESTRUCTURING_CLAUSE, UPFRONT_PAYMENT, FEE_SETTLEMENT_DATE, ACCCRUED_ON_DEFAULT);
    assertEquals(security.getSecurityType(), LegacyCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getBusinessDayConvention(), BDC);
    assertEquals(security.getCalendars(), CALENDARS);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getCouponFrequency(), COUPON_FREQUENCY);
    assertEquals(security.getDayCount(), DC);
    assertEquals(security.getDebtSeniority(), SENIORITY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getFeeSettlementDate(), FEE_SETTLEMENT_DATE);
    assertNull(security.getFixedRecovery());
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getName(), NAME);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getRestructuringClause(), RESTRUCTURING_CLAUSE);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUpfrontPayment(), UPFRONT_PAYMENT);
    assertEquals(security.isAccruedOnDefault(), ACCCRUED_ON_DEFAULT);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
  }

  /**
   * Tests that the accept() method calls the correct method in the visitor.
   */
  public void testAccept() {
    final LegacyCDSSecurity security = new LegacyCDSSecurity(IDS, NAME, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON,
        SENIORITY, COUPON_FREQUENCY, DC, BDC, CALENDARS, RESTRUCTURING_CLAUSE, UPFRONT_PAYMENT, FEE_SETTLEMENT_DATE, ACCCRUED_ON_DEFAULT);
    assertEquals(security.accept(TestVisitor.INSTANCE), NAME);
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
      return security.getName();
    }
  }
}
