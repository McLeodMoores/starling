/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.fra;

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
 * Tests for {@link FRASecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FRASecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.AUD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.AU);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2020, 4, 1);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2020, 1, 3);
  private static final double RATE = 0.001;
  private static final double AMOUNT = 1000000;
  private static final ExternalId UNDERLYING_ID = ExternalId.of("index", "3M IBOR");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FRASecurity.class,
        Arrays.asList("securityType", "currency", "regionId", "startDate", "endDate", "rate", "amount", "underlyingId", "fixingDate"),
        Arrays.asList(FRASecurity.SECURITY_TYPE, CCY, REGION, START_DATE, END_DATE, RATE, AMOUNT, UNDERLYING_ID, FIXING_DATE),
        Arrays.asList(ForwardRateAgreementSecurity.SECURITY_TYPE, Currency.BRL, UNDERLYING_ID, END_DATE, START_DATE, AMOUNT, RATE, REGION, START_DATE));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    FRASecurity fra = new FRASecurity();
    assertEquals(fra.getSecurityType(), FRASecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), 0.);
    assertNull(fra.getCurrency());
    assertNull(fra.getEndDate());
    assertNull(fra.getFixingDate());
    assertEquals(fra.getRate(), 0.);
    assertNull(fra.getRegionId());
    assertNull(fra.getStartDate());
    assertNull(fra.getUnderlyingId());
    fra = new FRASecurity(CCY, REGION, START_DATE, END_DATE, RATE, AMOUNT, UNDERLYING_ID, FIXING_DATE);
    assertEquals(fra.getSecurityType(), FRASecurity.SECURITY_TYPE);
    assertEquals(fra.getAmount(), AMOUNT);
    assertEquals(fra.getCurrency(), CCY);
    assertEquals(fra.getEndDate(), END_DATE);
    assertEquals(fra.getFixingDate(), FIXING_DATE);
    assertEquals(fra.getRate(), RATE);
    assertEquals(fra.getRegionId(), REGION);
    assertEquals(fra.getStartDate(), START_DATE);
    assertEquals(fra.getUnderlyingId(), UNDERLYING_ID);
  }

  /**
   * Tests that accept() calls the correct visitor method.
   */
  public void testAccept() {
    final FRASecurity fra = new FRASecurity(CCY, REGION, START_DATE, END_DATE, RATE, AMOUNT, UNDERLYING_ID, FIXING_DATE);
    assertEquals(fra.accept(TestVisitor.INSTANCE), "FRASecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFRASecurity(final FRASecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
