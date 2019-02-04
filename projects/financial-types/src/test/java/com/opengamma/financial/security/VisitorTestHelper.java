/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security;

import java.util.Set;

/**
 *
 */
public abstract class VisitorTestHelper {
  private Set<FinancialSecurity> _securities;

  // @BeforeTest
  // public void setUp() throws Exception {
  // final Reflections reflections = new Reflections();
  // final Set<Class<? extends FinancialSecurity>> classes =
  // reflections.getSubTypesOf(FinancialSecurity.class);
  // _securities = new HashSet<>();
  // for (final Class<? extends FinancialSecurity> clazz : classes) {
  // if (!Modifier.isAbstract(clazz.getModifiers()) &&
  // Modifier.isPublic(clazz.getModifiers())) {
  // final Constructor<? extends FinancialSecurity> constructor =
  // clazz.getConstructor();
  // constructor.setAccessible(true);
  // _securities.add(constructor.newInstance());
  // }
  // }
  // }
  //
  // protected Set<FinancialSecurity> getSecurities() {
  // return _securities;
  // }
}
