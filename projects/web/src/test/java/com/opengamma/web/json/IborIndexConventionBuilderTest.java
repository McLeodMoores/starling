/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.json;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link IborIndexConventionBuilder}.
 */
public class IborIndexConventionBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final IborIndexConvention convention = new IborIndexConvention("IBOR", ExternalIdBundle.of("TEST", "TEST"),
        DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, true, Currency.AUD, LocalTime.of(11, 0),
        "LONDON", ExternalId.of("TEST", "LONDON"), ExternalId.of("TEST", "NY"), "");
    assertEquals(convention, IborIndexConventionBuilder.INSTANCE.fromJSON(IborIndexConventionBuilder.INSTANCE.toJSON(convention)));
  }
}
