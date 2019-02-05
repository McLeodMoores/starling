/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.credit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link IndexCDSDefinitionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class IndexCDSDefinitionSecurityTest extends AbstractBeanTestCase {
  private static final LocalDate START_DATE = LocalDate.of(2020, 1, 1);
  private static final String VERSION = "v";
  private static final String SERIES = "s";
  private static final String FAMILY = "f";
  private static final Currency CCY = Currency.AUD;
  private static final Double RECOVERY_RATE = 0.35;
  private static final Frequency COUPON_FREQUENCY = SimpleFrequency.ANNUAL;
  private static final CDSIndexTerms TERMS = CDSIndexTerms.of(Tenor.TEN_YEARS);
  private static final CDSIndexComponentBundle COMPONENTS = CDSIndexComponentBundle
      .of(new CreditDefaultSwapIndexComponent("name", ExternalId.of("eid", "1"), 1., ExternalId.of("eid", "2")));
  private static final double COUPON = 0.01;
  private static final Set<ExternalId> CALENDARS = Collections.singleton(ExternalId.of("cal", "1"));
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "2");
  private static final String NAME = "name";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IndexCDSDefinitionSecurity.class,
        Arrays.asList("startDate", "version", "series", "family", "currency", "recoveryRate", "couponFrequency", "coupon", "terms", "components", "calendars",
            "businessDayConvention"),
        Arrays.asList(START_DATE, VERSION, SERIES, FAMILY, CCY, RECOVERY_RATE, COUPON_FREQUENCY, COUPON, TERMS, COMPONENTS, CALENDARS, BDC),
        Arrays.asList(START_DATE.plusDays(1), SERIES, FAMILY, VERSION, Currency.BRL, RECOVERY_RATE * 2, SimpleFrequency.BIMONTHLY, COUPON * 2,
            CDSIndexTerms.of(Tenor.ONE_YEAR),
            CDSIndexComponentBundle.of(new CreditDefaultSwapIndexComponent("name", ExternalId.of("eid", "3"), 1., ExternalId.of("eid", "4"))),
            Collections.singleton(ExternalId.of("cal", "2")), BusinessDayConventions.MODIFIED_FOLLOWING));
  }

  /**
   * Tests that the fields are set in the constructor.
   */
  public void testConstructor() {
    IndexCDSDefinitionSecurity security = new IndexCDSDefinitionSecurity();
    assertEquals(security.getSecurityType(), IndexCDSDefinitionSecurity.SECURITY_TYPE);
    assertNull(security.getBusinessDayConvention());
    assertNull(security.getCalendars());
    assertNull(security.getComponents());
    assertNull(security.getCoupon());
    assertNull(security.getCouponFrequency());
    assertNull(security.getCurrency());
    assertTrue(security.getExternalIdBundle().isEmpty());
    assertNull(security.getFamily());
    assertEquals(security.getName(), "");
    assertNull(security.getRecoveryRate());
    assertNull(security.getSeries());
    assertNull(security.getStartDate());
    assertNull(security.getTerms());
    assertNull(security.getVersion());
    security = new IndexCDSDefinitionSecurity(IDS, START_DATE, VERSION, SERIES, FAMILY, CCY, RECOVERY_RATE, COUPON_FREQUENCY, COUPON, TERMS, COMPONENTS,
        CALENDARS, BDC);
    assertEquals(security.getSecurityType(), IndexCDSDefinitionSecurity.SECURITY_TYPE);
    assertEquals(security.getBusinessDayConvention(), BDC);
    assertEquals(security.getCalendars(), CALENDARS);
    assertEquals(security.getComponents(), COMPONENTS);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getCouponFrequency(), COUPON_FREQUENCY);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getFamily(), FAMILY);
    assertEquals(security.getName(), "");
    assertEquals(security.getRecoveryRate(), RECOVERY_RATE);
    assertEquals(security.getSeries(), SERIES);
    assertEquals(security.getStartDate(), START_DATE);
    assertEquals(security.getTerms(), TERMS);
    assertEquals(security.getVersion(), VERSION);
    security = new IndexCDSDefinitionSecurity(IDS, NAME, START_DATE, VERSION, SERIES, FAMILY, CCY, RECOVERY_RATE, COUPON_FREQUENCY, COUPON, TERMS, COMPONENTS,
        CALENDARS, BDC);
    assertEquals(security.getSecurityType(), IndexCDSDefinitionSecurity.SECURITY_TYPE);
    assertEquals(security.getBusinessDayConvention(), BDC);
    assertEquals(security.getCalendars(), CALENDARS);
    assertEquals(security.getComponents(), COMPONENTS);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getCouponFrequency(), COUPON_FREQUENCY);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getFamily(), FAMILY);
    assertEquals(security.getName(), NAME);
    assertEquals(security.getRecoveryRate(), RECOVERY_RATE);
    assertEquals(security.getSeries(), SERIES);
    assertEquals(security.getStartDate(), START_DATE);
    assertEquals(security.getTerms(), TERMS);
    assertEquals(security.getVersion(), VERSION);
  }

  /**
   * Tests that the accept() method calls the correct method in the visitor.
   */
  public void testAccept() {
    final IndexCDSDefinitionSecurity security = new IndexCDSDefinitionSecurity(IDS, NAME, START_DATE, VERSION, SERIES, FAMILY, CCY, RECOVERY_RATE,
        COUPON_FREQUENCY, COUPON, TERMS, COMPONENTS, CALENDARS, BDC);
    assertEquals(security.accept(TestVisitor.INSTANCE), NAME);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitIndexCDSDefinitionSecurity(final IndexCDSDefinitionSecurity security) {
      return security.getName();
    }
  }

}
