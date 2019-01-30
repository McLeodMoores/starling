/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexDefinitionSecurityFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final CreditDefaultSwapIndexDefinitionSecurity CDS_INDEX_DEFINITION_SECURITY;
  static {
    final CreditDefaultSwapIndexComponent component1 =
        new CreditDefaultSwapIndexComponent("A", ExternalSchemes.markItRedCode("SZRTY"), 10.5, ExternalSchemes.isinSecurityId("ABC3456"));
    final CreditDefaultSwapIndexComponent component2 =
        new CreditDefaultSwapIndexComponent("B", ExternalSchemes.markItRedCode("ERT234"), 5.7, ExternalSchemes.isinSecurityId("ABC7890"));
    final CDSIndexComponentBundle components = CDSIndexComponentBundle.of(component1, component2);
    final CreditDefaultSwapIndexDefinitionSecurity security = new CreditDefaultSwapIndexDefinitionSecurity("1", "5", "CDX", Currency.USD, 0.4,
        CDSIndexTerms.of(Tenor.ONE_WEEK, Tenor.ONE_YEAR),
        components);
    security.setName("TEST_CDSINDEX_SEC");
    security.addExternalId(ExternalSchemes.markItRedCode("CDXI234"));
    CDS_INDEX_DEFINITION_SECURITY = security;
  }

  @Test
  public void testCycle() {
    assertEquals(CDS_INDEX_DEFINITION_SECURITY, cycleObject(CreditDefaultSwapIndexDefinitionSecurity.class,
        CDS_INDEX_DEFINITION_SECURITY));
  }

}
