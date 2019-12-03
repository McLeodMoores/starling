/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.fx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
 * Tests for {@link NonDeliverableFXForwardSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class NonDeliverableFXForwardSecurityTest extends AbstractBeanTestCase {
  private static final Currency PAY_CURRENCY = Currency.AUD;
  private static final Currency RECEIVE_CURRENCY = Currency.BRL;
  private static final double PAY_AMOUNT = 1000;
  private static final double RECEIVE_AMOUNT = 7000;
  private static final ZonedDateTime FORWARD_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ExternalId REGION_ID = ExternalSchemes.countryRegionId(Country.AU);
  private static final boolean DELIVER_IN_RECEIVE = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(NonDeliverableFXForwardSecurity.class,
        Arrays.asList("payCurrency", "receiveCurrency", "payAmount", "receiveAmount", "forwardDate", "regionId", "deliverInReceiveCurrency"),
        Arrays.asList(PAY_CURRENCY, RECEIVE_CURRENCY, PAY_AMOUNT, RECEIVE_AMOUNT, FORWARD_DATE, REGION_ID, DELIVER_IN_RECEIVE), Arrays.asList(RECEIVE_CURRENCY,
            PAY_CURRENCY, RECEIVE_AMOUNT, PAY_AMOUNT, FORWARD_DATE.plusDays(1), ExternalSchemes.countryRegionId(Country.BR), !DELIVER_IN_RECEIVE));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    NonDeliverableFXForwardSecurity fx = new NonDeliverableFXForwardSecurity();
    assertNull(fx.getForwardDate());
    assertEquals(fx.getPayAmount(), 0.);
    assertNull(fx.getPayCurrency());
    assertEquals(fx.getReceiveAmount(), 0.);
    assertNull(fx.getReceiveCurrency());
    assertNull(fx.getRegionId());
    assertFalse(fx.isDeliverInReceiveCurrency());
    fx = new NonDeliverableFXForwardSecurity(PAY_CURRENCY, PAY_AMOUNT, RECEIVE_CURRENCY, RECEIVE_AMOUNT, FORWARD_DATE, REGION_ID, DELIVER_IN_RECEIVE);
    assertEquals(fx.getForwardDate(), FORWARD_DATE);
    assertEquals(fx.getPayAmount(), PAY_AMOUNT);
    assertEquals(fx.getPayCurrency(), PAY_CURRENCY);
    assertEquals(fx.getReceiveAmount(), RECEIVE_AMOUNT);
    assertEquals(fx.getReceiveCurrency(), RECEIVE_CURRENCY);
    assertEquals(fx.getRegionId(), REGION_ID);
    assertTrue(fx.isDeliverInReceiveCurrency());
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final NonDeliverableFXForwardSecurity fx = new NonDeliverableFXForwardSecurity(PAY_CURRENCY, PAY_AMOUNT, RECEIVE_CURRENCY, RECEIVE_AMOUNT, FORWARD_DATE,
        REGION_ID, DELIVER_IN_RECEIVE);
    assertEquals(fx.accept(TestVisitor.INSTANCE), "NonDeliverableFXForwardSecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
