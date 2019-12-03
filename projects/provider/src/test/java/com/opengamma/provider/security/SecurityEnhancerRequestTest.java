/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.security;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityEnhancerRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityEnhancerRequestTest extends AbstractBeanTestCase {
  private static final List<Security> SECURITIES = Arrays.<Security> asList(new SimpleSecurity("name1"), new SimpleSecurity("name2"));

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final ArrayList<Security> reversed = new ArrayList<>(SECURITIES);
    Collections.reverse(reversed);
    return new JodaBeanProperties<>(SecurityEnhancerRequest.class,
        Arrays.asList("securities"), Arrays.asList(SECURITIES), Arrays.asList(reversed));
  }

  /**
   * Tests that the static constructors are equivalent.
   */
  public void testStaticConstructor() {
    final SecurityEnhancerRequest request = SecurityEnhancerRequest.create(SECURITIES);
    assertEquals(SecurityEnhancerRequest.create(SECURITIES.toArray(new Security[0])), request);
  }

  /**
   * Tests that the security array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullArray() {
    new SecurityEnhancerRequest().addSecurities((Security[]) null);
  }

  /**
   * Tests that the security iterable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullIterable() {
    new SecurityEnhancerRequest().addSecurities((Iterable<? extends Security>) null);
  }

  /**
   * Tests that securities are added to the existing list.
   */
  public void testAdd() {
    final SecurityEnhancerRequest request = new SecurityEnhancerRequest();
    assertEquals(request.getSecurities().size(), 0);
    request.addSecurities(SECURITIES);
    assertEquals(request.getSecurities(), SECURITIES);
    final Security[] securities = new Security[] { new SimpleSecurity("name3"), new SimpleSecurity("name4") };
    request.addSecurities(securities);
    assertEquals(request.getSecurities(), Arrays.asList(SECURITIES.get(0), SECURITIES.get(1), securities[0], securities[1]));
  }

}
