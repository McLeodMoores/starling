/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class TestUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

  /**
   * Reflective testing of builder methods - assumes that any method that returns the same type is
   * a builder method, and that null inputs are invalid for all.
   * @param clazz  the builder to be tested, not null
   * @param parent  the parent class containing the method declarations to be tested, not null
   * @param methodAcceptsNull  the names of any methods that can accept null
   */
  public static void testNullBuilderMethodInputs(final Class<?> clazz, final Class<?> parent, final String... methodAcceptsNull) {
    try {
      final Method[] methods = parent.getDeclaredMethods();
      for (final Method method : methods) {
        if (methodAcceptsNull != null && methodAcceptsNull.length > 0 && Arrays.binarySearch(methodAcceptsNull, method.getName()) < 0) {
          continue;
        }
        if (method.getReturnType().isAssignableFrom(clazz) && method.getParameterTypes().length == 1) {
          if (method.getParameterTypes()[0].isPrimitive()) {
            LOGGER.warn("Method {} in {} has a primitive input type, not checking input for null", method.getName(), clazz.getSimpleName());
            continue;
          }
          final Object[] args = new Object[method.getParameterTypes().length]; // initialized with nulls
          // create a new object
          final Object builder = clazz.newInstance(); // relying on no-args constructor - should pass in list of arguments
            try {
            method.invoke(builder, args);
            fail("Method called " + method.getName() + " should check that the input is not null");
          } catch (final InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            assertTrue(cause instanceof IllegalArgumentException,
                method.getName() + "(" + Arrays.toString(method.getParameters()) + ") in " + clazz.getSimpleName()
                  + " threw " + cause + "(" + cause.getMessage() + "): ");
            assertTrue(cause.getMessage().startsWith("Input parameter"));
          } catch (final Exception e) {
            fail(method.getName() + ": " + e);
            e.printStackTrace();
          }
        }
      }
    } catch (final IllegalAccessException e) {
      fail(e.getMessage());
    } catch (final InstantiationException e) {
      fail(e.getMessage());
    }
  }

  private TestUtils() {
  }
}
