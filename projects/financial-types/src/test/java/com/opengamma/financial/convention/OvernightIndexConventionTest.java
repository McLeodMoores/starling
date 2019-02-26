/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link OvernightIndexConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class OvernightIndexConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EID = ExternalIdBundle.of("eid", "1");
  private static final DayCount DC = DayCounts.ACT_360;
  private static final int PUBLICATION_LAG = 1;
  private static final Currency CCY = Currency.USD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final OvernightIndexConvention CONVENTION = new OvernightIndexConvention(NAME, EID, DC, PUBLICATION_LAG, CCY, REGION);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(OvernightIndexConvention.class,
        Arrays.asList("name", "externalIdBundle", "dayCount", "publicationLag", "currency", "regionCalendar"),
        Arrays.<Object> asList(NAME, EID, DC, PUBLICATION_LAG, CCY, REGION), Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), DayCounts.ACT_365,
            PUBLICATION_LAG + 1, Currency.AUD, ExternalSchemes.countryRegionId(Country.AU)));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), OvernightIndexConvention.TYPE);
  }

  /**
   * Tests that the accept() method visits the right method.
   */
  public void testVisitor() {
    assertEquals(CONVENTION.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialConventionVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitOvernightIndexConvention(final OvernightIndexConvention convention) {
      return "visited";
    }

  }

}
