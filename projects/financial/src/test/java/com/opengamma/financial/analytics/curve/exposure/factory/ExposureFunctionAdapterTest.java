/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.analytics.curve.exposure.CounterpartyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityAndSettlementExchangeExposureFunction;
import com.opengamma.financial.testutils.SecurityInstances;
import com.opengamma.id.ExternalId;

/**
 * Unit tests for {@link ExposureFunctionAdapter}.
 */
public class ExposureFunctionAdapterTest {

  /**
   * Tests the behaviour when the exposure function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new ExposureFunctionAdapter(null);
  }

  /**
   * Tests the wrapping.
   */
  @Test
  public void test() {
    final CounterpartyExposureFunction exposureFunction = new CounterpartyExposureFunction();
    final NamedExposureFunction adapter = new ExposureFunctionAdapter(exposureFunction);
    assertEquals(new ExposureFunctionAdapter(new CounterpartyExposureFunction()), adapter);
    assertEquals(new ExposureFunctionAdapter(exposureFunction).hashCode(), adapter.hashCode());
    assertNotEquals(new ExposureFunctionAdapter(new SecurityAndSettlementExchangeExposureFunction()), adapter);
    assertEquals(adapter.getName(), CounterpartyExposureFunction.NAME);
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(SecurityInstances.VANILLA_IBOR_SWAP));
    trade.setCounterparty(new SimpleCounterparty(ExternalId.of("Ctpty", "OTHER")));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("Ctpty", "OTHER"));
    assertEquals(adapter.getIds(trade), expected);
    assertEquals(adapter.getIds(trade, new FunctionCompilationContext()), expected);
    assertEquals(adapter.getIds(trade, new FunctionExecutionContext()), expected);
  }
}
