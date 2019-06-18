/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.Collections;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.testng.annotations.Test;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityToFixedIncomeFutureDefinitionConverterTest {

  private static final HolidaySource HOLIDAY_SOURCE = new WeekendHolidaySource();
  private static final ExchangeSource EXCHANGE_SOURCE = myExchangeSource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(
      new InMemoryConventionBundleMaster());

  @Test
  public void test() {
    //TODO
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static ExchangeSource myExchangeSource() {
    final Exchange exchange = Mockito.mock(Exchange.class);
    Mockito.when(exchange.getUniqueId()).thenReturn(UniqueId.of("SOMETHING", "SOMETHING ELSE"));
    final ExchangeSource source = Mockito.mock(ExchangeSource.class);
    Mockito.when(source.get(Matchers.any(UniqueId.class))).thenReturn(exchange);
    Mockito.when(source.get(Matchers.any(ObjectId.class), Matchers.any(VersionCorrection.class))).thenReturn(exchange);
    ((OngoingStubbing) Mockito.when(source.get(Matchers.any(ExternalIdBundle.class), Matchers.any(VersionCorrection.class)))).thenReturn(Collections.singleton(exchange));
    Mockito.when(source.getSingle(Matchers.any(ExternalId.class))).thenReturn(exchange);
    Mockito.when(source.getSingle(Matchers.any(ExternalIdBundle.class))).thenReturn(exchange);
    return source;
  }

}
