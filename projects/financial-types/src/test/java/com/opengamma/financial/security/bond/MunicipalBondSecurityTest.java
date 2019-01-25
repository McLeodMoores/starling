/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.bond;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link MunicipalBondSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class MunicipalBondSecurityTest extends AbstractBeanTestCase {
  private static final String ISSUER_NAME = "NY";
  private static final String ISSUER_TYPE = "City";
  private static final String ISSUER_DOMICILE = "US";
  private static final String MARKET = "Muni";
  private static final Currency CURRENCY = Currency.USD;
  private static final YieldConvention YIELD = SimpleYieldConvention.US_STREET;
  private static final Expiry LAST_TRADE_DATE = new Expiry(DateUtils.getUTCDate(2030, 12, 15));
  private static final String COUPON_TYPE = "Fixed";
  private static final double COUPON_RATE = 0.12;
  private static final Frequency FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final DayCount DAY_COUNT = DayCounts.ACT_365;
  private static final ZonedDateTime INTEREST_ACCRUAL = DateUtils.getUTCDate(2010, 12, 15);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2010, 12, 16);
  private static final ZonedDateTime FIRST_COUPON = DateUtils.getUTCDate(2011, 6, 15);
  private static final Double ISSUANCE_PRICE = 100.;
  private static final double AMOUNT_ISSUED = 1000000;
  private static final double MINIMUM_AMOUNT = 1000;
  private static final double MINIMUM_INCREMENT = 250;
  private static final double PAR_AMOUNT = 1;
  private static final double REDEMPTION_VALUE = 0.99;

  @DataProvider(name = "propertyValues")
  @Override
  public Object[][] propertyValues() {
    return new Object[][] { { new JodaBeanProperties<>(MunicipalBondSecurity.class,
        Arrays.asList("issuerName", "issuerType", "issuerDomicile", "market", "currency", "yieldConvention", "lastTradeDate", "couponType", "couponRate",
            "couponFrequency", "dayCount", "interestAccrualDate", "settlementDate", "firstCouponDate", "issuancePrice", "totalAmountIssued",
            "minimumAmount", "minimumIncrement", "parAmount", "redemptionValue"),
        Arrays.asList(ISSUER_NAME, ISSUER_TYPE, ISSUER_DOMICILE, MARKET, CURRENCY, YIELD, LAST_TRADE_DATE, COUPON_TYPE, COUPON_RATE, FREQUENCY, DAY_COUNT,
            INTEREST_ACCRUAL, SETTLEMENT_DATE, FIRST_COUPON, ISSUANCE_PRICE, AMOUNT_ISSUED, MINIMUM_AMOUNT, MINIMUM_INCREMENT, PAR_AMOUNT,
            REDEMPTION_VALUE),
        Arrays.asList(ISSUER_TYPE, ISSUER_NAME, MARKET, ISSUER_DOMICILE, Currency.AUD, SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND,
            new Expiry(DateUtils.getUTCDate(2040, 12, 15)), "Float", COUPON_RATE + 1, SimpleFrequency.ANNUAL, DayCounts.ACT_360, INTEREST_ACCRUAL.plusDays(1),
            SETTLEMENT_DATE.plusDays(1), FIRST_COUPON.plusDays(1), ISSUANCE_PRICE + 1, AMOUNT_ISSUED + 1, MINIMUM_AMOUNT + 1, MINIMUM_INCREMENT + 1,
            PAR_AMOUNT + 1, REDEMPTION_VALUE + 1)) } };
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    final MunicipalBondSecurity security = new MunicipalBondSecurity(ISSUER_NAME, ISSUER_TYPE, ISSUER_DOMICILE, MARKET, CURRENCY, YIELD, LAST_TRADE_DATE,
        COUPON_TYPE, COUPON_RATE, FREQUENCY, DAY_COUNT, INTEREST_ACCRUAL, SETTLEMENT_DATE, FIRST_COUPON, ISSUANCE_PRICE, AMOUNT_ISSUED, MINIMUM_AMOUNT,
        MINIMUM_INCREMENT, PAR_AMOUNT, REDEMPTION_VALUE);
    assertEquals(security.accept(TestVisitor.INSTANCE), "visited");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
      return "visited";
    }

    @Override
    public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return "not visited";
    }

  }

}
