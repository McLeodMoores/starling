/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.mcleodmoores.web.json.convention.DepositConventionJsonBuilder;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link DepositConventionJsonBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class DepositConventionJsonBuilderTest {

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final DepositConvention convention = new DepositConvention("GB", externalIds, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, false, Currency.USD,
        ExternalSchemes.countryRegionId(Country.US));
    convention.setAttributes(attributes);
    assertEquals(convention, DepositConventionJsonBuilder.INSTANCE.fromJSON(DepositConventionJsonBuilder.INSTANCE.toJSON(convention)));
    // template convention
    final String conventionJson = DepositConventionJsonBuilder.INSTANCE.getTemplate();
    assertEquals(conventionJson, DepositConventionJsonBuilder.INSTANCE.toJSON(DepositConventionJsonBuilder.INSTANCE.fromJSON(conventionJson)));
  }

  /**
   * Tests the copy.
   */
  @Test
  public void testCopy() {
    final ExternalIdBundle externalIds = ExternalIdBundle.of("TEST", "TEST");
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final DepositConvention convention = new DepositConvention("GB", externalIds, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, false, Currency.USD,
        ExternalSchemes.countryRegionId(Country.US));
    final DepositConvention copy = DepositConventionJsonBuilder.INSTANCE.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new DepositConvention("GB", externalIds, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 2, false, Currency.USD,
        ExternalSchemes.countryRegionId(Country.US)));
  }
}
