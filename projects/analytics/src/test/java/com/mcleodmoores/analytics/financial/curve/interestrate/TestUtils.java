/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

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
   * @param constructorArguments  any arguments for the constructor, null or empty means the no-arg constructor will be used
   * @param methodAcceptsNull  the names of any methods that can accept null
   */
  public static void testNullBuilderMethodInputs(final Class<?> clazz, final Class<?> parent, final Object[] constructorArguments,
      final String... methodAcceptsNull) {
    final Method[] methods = parent.getDeclaredMethods();
    for (final Method method : methods) {
      if (methodAcceptsNull != null && methodAcceptsNull.length > 0 && Arrays.binarySearch(methodAcceptsNull, method.getName()) >= 0) {
        continue;
      }
      if (method.getReturnType().isAssignableFrom(clazz) && method.getParameterTypes().length == 1) {
        if (method.getParameterTypes()[0].isPrimitive()) {
          LOGGER.warn("Method {} in {} has a primitive input type, not checking input for null", method.getName(), clazz.getSimpleName());
          continue;
        }
        final Object builder = createBuilder(clazz, constructorArguments);
        if (builder == null) {
          fail("An instance of " + clazz.getSimpleName() + " could not be created with the arguments " + Arrays.toString(constructorArguments));
        }
        final Object[] args = new Object[1]; // initialized with nulls
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
  }

  public static void testEmptyBuilderMethodInputs(final Class<?> clazz, final Class<?> parent, final Object[] constructorArguments,
      final String... methodAcceptsEmpty) {
      final Method[] methods = parent.getDeclaredMethods();
    for (final Method method : methods) {
      if (methodAcceptsEmpty != null && methodAcceptsEmpty.length > 0 && Arrays.binarySearch(methodAcceptsEmpty, method.getName()) >= 0) {
        continue;
      }
      if (method.getReturnType().isAssignableFrom(clazz) && method.getParameterTypes().length == 1) {
        final Object builder = createBuilder(clazz, constructorArguments);
        if (builder == null) {
          fail("An instance of " + clazz.getSimpleName() + " could not be created with the arguments " + Arrays.toString(constructorArguments));
        }
        final Class<?> parameter = method.getParameterTypes()[0];
        final Object[] args = new Object[1];
        try {
          if (Collection.class.isAssignableFrom(parameter)) {
            args[0] = parameter.newInstance();
          } else if (parameter.isArray()) {
            final Class<?> arrayType = parameter.getComponentType();
            args[0] = Array.newInstance(arrayType, 0);
          } else {
            continue;
          }
        } catch (InstantiationException | IllegalAccessException e) {
          fail(e.getMessage());
        }
        try {
          method.invoke(builder, args);
          fail("Method called " + method.getName() + " should check that the input is not empty");
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
  }

  private static Object createBuilder(final Class<?> clazz, final Object[] constructorArguments) {
    Object builder = null;
    try {
      if (constructorArguments == null || constructorArguments.length == 0) {
        builder = clazz.newInstance();
      } else {
        final Constructor<?>[] constructors = clazz.getConstructors();
        for (final Constructor<?> constructor : constructors) {
          if (constructor.getParameters().length == constructorArguments.length) {
            try {
              builder = constructor.newInstance(constructorArguments);
              break;
            } catch (final InstantiationException | InvocationTargetException  | IllegalAccessException | IllegalArgumentException e) {
              // carry on trying to find matching constructor
            }
          }
        }
      }
    } catch (final IllegalAccessException | InstantiationException e) {
    }
    return builder;
  }

  private TestUtils() {
  }
}
