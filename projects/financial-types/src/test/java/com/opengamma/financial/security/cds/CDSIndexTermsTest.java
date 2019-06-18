/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.cds;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link CDSIndexTerms}.
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexTermsTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final SortedSet<Tenor> values = new TreeSet<>();
    values.add(Tenor.EIGHT_MONTHS);
    values.add(Tenor.EIGHT_YEARS);
    final SortedSet<Tenor> otherValues = new TreeSet<>();
    values.add(Tenor.EIGHTEEN_MONTHS);
    values.add(Tenor.FIVE_YEARS);
    return new JodaBeanProperties<>(CDSIndexTerms.class, Arrays.asList("tenors"), Arrays.asList(values), Arrays.asList(otherValues));
  }

  /**
   * Tests constructor equivalence.
   */
  public void testConstructor() {
    final CDSIndexTerms terms = CDSIndexTerms.of(Tenor.EIGHT_MONTHS);
    assertEquals(terms.getTenors(), ImmutableSortedSet.of(Tenor.EIGHT_MONTHS));
    final SortedSet<Tenor> values = new TreeSet<>();
    values.add(Tenor.EIGHT_MONTHS);
    CDSIndexTerms usingIterable = CDSIndexTerms.of(values);
    CDSIndexTerms usingVarargs = CDSIndexTerms.of(Tenor.EIGHT_MONTHS);
    assertEquals(terms, usingIterable);
    assertEquals(terms, usingVarargs);
    values.add(Tenor.EIGHT_YEARS);
    values.add(Tenor.TEN_YEARS);
    usingIterable = CDSIndexTerms.of(values);
    usingVarargs = CDSIndexTerms.of(Tenor.EIGHT_MONTHS, Tenor.EIGHT_YEARS, Tenor.TEN_YEARS);
    assertEquals(usingIterable, usingVarargs);
  }
}
