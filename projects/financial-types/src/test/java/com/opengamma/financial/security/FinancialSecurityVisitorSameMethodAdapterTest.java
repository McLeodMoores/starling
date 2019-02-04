/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security;

/**
 *
 */
public class FinancialSecurityVisitorSameMethodAdapterTest extends VisitorTestHelper {

  // public void test() {
  // final Set<FinancialSecurity> securities = getSecurities();
  // final FinancialSecurityVisitorSameMethodAdapter<String> visitor = new
  // FinancialSecurityVisitorSameMethodAdapter<>(TestVisitor.INSTANCE);
  // for (final FinancialSecurity security : securities) {
  // assertEquals(security.accept(visitor),
  // security.getClass().getSimpleName());
  // }
  // }
  //
  // private static class TestVisitor implements
  // FinancialSecurityVisitorSameMethodAdapter.Visitor<String> {
  // public static final TestVisitor INSTANCE = new TestVisitor();
  //
  // private TestVisitor() {
  // }
  //
  // @Override
  // public String visit(final FinancialSecurity security) {
  // return security.getClass().getSimpleName();
  // }
  //
  // }
}
