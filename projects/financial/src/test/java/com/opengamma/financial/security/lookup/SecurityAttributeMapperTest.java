/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import static com.opengamma.financial.security.lookup.SecurityAttribute.MATURITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.PRODUCT;
import static com.opengamma.financial.security.lookup.SecurityAttribute.QUANTITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.RATE;
import static com.opengamma.financial.security.lookup.SecurityAttribute.TYPE;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityAttributeMapperTest {

  private static final CurrencyPairs CURRENCY_PAIRS = CurrencyPairs.of(ImmutableSet.of(CurrencyPair.of(Currency.GBP,
                                                                                                        Currency.USD)));
  private static final SecurityAttributeMapper DEFAULT_MAPPINGS = DefaultSecurityAttributeMappings.create(CURRENCY_PAIRS);

  /**
   * Simple security where fields are mapped using bean properties.
   */
  @Test
  public void fra() {
    final ExternalId regionId = ExternalId.of("Reg", "123");
    final ExternalId underlyingId = ExternalId.of("Und", "321");
    final ZonedDateTime startDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime endDate = zdt(2013, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime fixingDate = zdt(2013, 12, 20, 11, 0, 0, 0, ZoneOffset.UTC);
    final FRASecurity security = new FRASecurity(Currency.AUD, regionId, startDate, endDate, 0.1, 1000, underlyingId, fixingDate);
    assertEquals("FRA", DEFAULT_MAPPINGS.valueFor(TYPE, security));
    assertEquals(Currency.AUD, DEFAULT_MAPPINGS.valueFor(PRODUCT, security));
    assertEquals(1000d, DEFAULT_MAPPINGS.valueFor(QUANTITY, security));
  }

  /**
   * Custom providers for values derived from multiple security properties
   */
  @Test
  public void fxForward() {
    final ZonedDateTime forwardDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ExternalId regionId = ExternalId.of("Reg", "123");
    final FXForwardSecurity security = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    assertEquals("FX Forward", DEFAULT_MAPPINGS.valueFor(TYPE, security));
    assertEquals("GBP/USD", DEFAULT_MAPPINGS.valueFor(PRODUCT, security));
    assertEquals(forwardDate, DEFAULT_MAPPINGS.valueFor(MATURITY, security));
    final FXAmounts expected = FXAmounts.forForward(security.getPayCurrency(),
                                              security.getReceiveCurrency(),
                                              security.getPayAmount(),
                                              security.getReceiveAmount(),
                                              CURRENCY_PAIRS);
    assertEquals(expected, DEFAULT_MAPPINGS.valueFor(QUANTITY, security));
    assertEquals(1.5d, DEFAULT_MAPPINGS.valueFor(RATE, security));
  }

  /**
   * if no columns are mapped for a class then it should inherit mappings set up for its superclasses
   */
  @Test
  public void inheritSuperclassMappings() {
    class A extends ManageableSecurity {
      private static final long serialVersionUID = 1L;
    }
    class B extends A {
      private static final long serialVersionUID = 1L;
    }
    class C extends B {
      private static final long serialVersionUID = 1L;
    }
    final SecurityAttributeMapper mapper = new SecurityAttributeMapper();
    final String aType = "A type";
    final String bProduct = "B product";
    mapper.mapColumn(TYPE, A.class, aType);
    mapper.mapColumn(PRODUCT, B.class, bProduct);
    final C c = new C();

    // check the case where there are no columns mapped for a subtype
    assertEquals(aType, mapper.valueFor(TYPE, c));
    assertEquals(bProduct, mapper.valueFor(PRODUCT, c));

    // add a mapping for the subtype and check the supertype mappings are still picked up
    final String cMaturity = "C maturity";
    mapper.mapColumn(MATURITY, C.class, cMaturity);

    assertEquals(aType, mapper.valueFor(TYPE, c));
    assertEquals(bProduct, mapper.valueFor(PRODUCT, c));
    assertEquals(cMaturity, mapper.valueFor(MATURITY, c));

    // check overriding works
    final String cType = "C type";
    mapper.mapColumn(TYPE, C.class, cType);
    assertEquals(cType, mapper.valueFor(TYPE, c));
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
