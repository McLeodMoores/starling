/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.fx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link FXForwardSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FXForwardSecurityTest extends AbstractBeanTestCase {
  private static final Currency PAY_CURRENCY = Currency.AUD;
  private static final Currency RECEIVE_CURRENCY = Currency.BRL;
  private static final double PAY_AMOUNT = 1000;
  private static final double RECEIVE_AMOUNT = 7000;
  private static final ZonedDateTime FORWARD_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ExternalId REGION_ID = ExternalSchemes.countryRegionId(Country.AU);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXForwardSecurity.class,
        Arrays.asList("payCurrency", "receiveCurrency", "payAmount", "receiveAmount", "forwardDate", "regionId"),
        Arrays.asList(PAY_CURRENCY, RECEIVE_CURRENCY, PAY_AMOUNT, RECEIVE_AMOUNT, FORWARD_DATE, REGION_ID),
        Arrays.asList(RECEIVE_CURRENCY, PAY_CURRENCY, RECEIVE_AMOUNT, PAY_AMOUNT, FORWARD_DATE.plusDays(1), ExternalSchemes.countryRegionId(Country.BR)));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    FXForwardSecurity fx = new FXForwardSecurity();
    assertNull(fx.getForwardDate());
    assertEquals(fx.getPayAmount(), 0.);
    assertNull(fx.getPayCurrency());
    assertEquals(fx.getReceiveAmount(), 0.);
    assertNull(fx.getReceiveCurrency());
    assertNull(fx.getRegionId());
    fx = new FXForwardSecurity(PAY_CURRENCY, PAY_AMOUNT, RECEIVE_CURRENCY, RECEIVE_AMOUNT, FORWARD_DATE, REGION_ID);
    assertEquals(fx.getForwardDate(), FORWARD_DATE);
    assertEquals(fx.getPayAmount(), PAY_AMOUNT);
    assertEquals(fx.getPayCurrency(), PAY_CURRENCY);
    assertEquals(fx.getReceiveAmount(), RECEIVE_AMOUNT);
    assertEquals(fx.getReceiveCurrency(), RECEIVE_CURRENCY);
    assertEquals(fx.getRegionId(), REGION_ID);
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final FXForwardSecurity fx = new FXForwardSecurity(PAY_CURRENCY, PAY_AMOUNT, RECEIVE_CURRENCY, RECEIVE_AMOUNT, FORWARD_DATE, REGION_ID);
    assertEquals(fx.accept(TestVisitor.INSTANCE), "FXForwardSecurity");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFXForwardSecurity(final FXForwardSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
