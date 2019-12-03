/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static com.opengamma.web.WebResourceTestUtils.assertJSONObjectEquals;
import static org.testng.Assert.fail;

import java.util.List;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link WebSecurityResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebSecurityResourceTest extends AbstractWebSecurityResourceTestCase {

  @Test
  @Override
  public void testEquitySecurity() {
    assertGetSecurity(_securities.get(EquitySecurity.class));
  }

  @Test
  @Override
  public void testBondFutureSecurity() {
    assertGetSecurity(_securities.get(BondFutureSecurity.class));
  }

  private void assertGetSecurity(final List<FinancialSecurity> securities) {
    for (final FinancialSecurity security : securities) {
      final WebSecurityResource securityResource = _webSecuritiesResource.findSecurity(security.getUniqueId().toString());
      try {
        final JSONObject actualJson = new JSONObject(securityResource.getJSON());

        final JSONObject expectedJson = security.accept(new ExpectedSecurityJsonProvider());
        assertJSONObjectEquals(expectedJson, actualJson);
      } catch (final Exception e) {
        fail(e.getMessage());
      }
    }
  }

}
