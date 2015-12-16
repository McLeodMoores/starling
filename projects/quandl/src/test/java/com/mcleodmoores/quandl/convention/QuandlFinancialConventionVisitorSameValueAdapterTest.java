/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitor;
import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitorSameValueAdapter;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitor;

/**
 * Unit tests for {@link QuandlFinancialConventionVisitorSameValueAdapter}.
 */
public class QuandlFinancialConventionVisitorSameValueAdapterTest {
  /** The result */
  private static final Integer RESULT = 2;
  /** The visitor */
  private static final FinancialConventionVisitor<Integer> VISITOR = new QuandlFinancialConventionVisitorSameValueAdapter<>(RESULT);

  /**
   * Tests that the result is returned for all methods. Reflection is used to ensure that every financial
   * convention type is tested. This test method is intended to find the case where methods have been automatically
   * created but not implemented. There is also a cross-check that there is one convention of each type in the
   * instances set from {@link ConventionTestInstances}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConventions() {
    final Class<?> clazz = QuandlFinancialConventionVisitor.class;
    final Method[] methods = clazz.getMethods();
    final Set<Class<FinancialConvention>> conventions = new HashSet<>();
    int count = 0;
    for (final Method method : methods) {
      if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith("visit")) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
          throw new IllegalStateException("Expecting the methods of QuandlFinancialConventionVisitor to have one argument");
        }
        conventions.add((Class<FinancialConvention>) parameterTypes[0]);
        count++;
      }
    }
    assertEquals(count, ConventionTestInstances.INSTANCES.size());
    final Iterator<FinancialConvention> instancesIter = ConventionTestInstances.INSTANCES.iterator();
    while (instancesIter.hasNext()) {
      final FinancialConvention convention = instancesIter.next();
      assertTrue(conventions.contains(convention.getClass()));
      assertEquals(convention.accept(VISITOR), RESULT);
    }
  }

}
