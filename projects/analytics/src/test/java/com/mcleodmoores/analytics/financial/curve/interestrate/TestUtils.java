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
   * Reflective testing of builder methods. Assumes that any method that returns the same type is
   * a builder method, and that null inputs are invalid for all.
   * @param clazz  the builder to be tested, not null
   * @param methodAcceptsNull  the names of any methods that can accept null
   */
  public static void testNullBuilderMethodInputs(final Class<?> clazz, final String... methodAcceptsNull) {
    testNullBuilderMethodInputs(clazz, clazz, null, methodAcceptsNull);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is
   * a builder method, and that null inputs are invalid for all.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  any arguments for the constructor, null or empty means the no-arg constructor will be used
   * @param methodAcceptsNull  the names of any methods that can accept null
   */
  public static void testNullBuilderMethodInputs(final Class<?> clazz, final Object[] constructorArguments, final String... methodAcceptsNull) {
    testNullBuilderMethodInputs(clazz, clazz, constructorArguments, methodAcceptsNull);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is
   * a builder method, and that null inputs are invalid for all.
   * @param clazz  the builder to be tested, not null
   * @param parent  the parent class containing the method declarations to be tested, not null
   * @param methodAcceptsNull  the names of any methods that can accept null
   */
  public static void testNullBuilderMethodInputs(final Class<?> clazz, final Class<?> parent, final String... methodAcceptsNull) {
    testNullBuilderMethodInputs(clazz, parent, null, methodAcceptsNull);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is
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

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is a builder
   * method, and that any collection / array inputs cannot be empty.
   * @param clazz  the builder to be tested, not null
   * @param methodAcceptsEmpty  the names of any methods that can accept empty collections or arrays
   */
  public static void testEmptyBuilderMethodInputs(final Class<?> clazz, final String... methodAcceptsEmpty) {
    testEmptyBuilderMethodInputs(clazz, clazz, null, methodAcceptsEmpty);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is a builder
   * method, and that any collection / array inputs cannot be empty.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  any arguments for the constructor, null or empty means the no-arg constructor will be used
   * @param methodAcceptsEmpty  the names of any methods that can accept empty collections or arrays
   */
  public static void testEmptyBuilderMethodInputs(final Class<?> clazz, final Object[] constructorArguments, final String... methodAcceptsEmpty) {
    testEmptyBuilderMethodInputs(clazz, clazz, constructorArguments, methodAcceptsEmpty);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is a builder
   * method, and that any collection / array inputs cannot be empty.
   * @param clazz  the builder to be tested, not null
   * @param parent  the parent class containing the method declarations to be tested, not null
   * @param methodAcceptsEmpty  the names of any methods that can accept empty collections or arrays
   */
  public static void testEmptyBuilderMethodInputs(final Class<?> clazz, final Class<?> parent, final String... methodAcceptsEmpty) {
    testEmptyBuilderMethodInputs(clazz, parent, null, methodAcceptsEmpty);
  }

  /**
   * Reflective testing of builder methods. Assumes that any method that returns the same type is a builder
   * method, and that any collection / array inputs cannot be empty.
   * @param clazz  the builder to be tested, not null
   * @param parent  the parent class containing the method declarations to be tested, not null
   * @param constructorArguments  any arguments for the constructor, null or empty means the no-arg constructor will be used
   * @param methodAcceptsEmpty  the names of any methods that can accept empty collections or arrays
   */
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

  /**
   * Tests that a numerical input to the methods must be greater than the lowest value provided.
   * @param clazz  the builder to be tested, not null
   * @param inclusive  true if the value can be equal to the lower range
   * @param lowerRange  the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsLowerRange(final Class<?> clazz, final double lowerRange, final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, null, lowerRange, inclusive, Double.POSITIVE_INFINITY, false, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be greater than the lowest value provided.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param lowerRange  the lower range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsLowerRange(final Class<?> clazz, final Object[] constructorArguments, final double lowerRange,
      final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, constructorArguments, lowerRange, inclusive, Double.POSITIVE_INFINITY, false, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be greater than the lowest value provided.
   * @param clazz  the builder to be tested, not null
   * @param parent  the class where the methods are declared, not null
   * @param lowerRange  the lower range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsLowerRange(final Class<?> clazz, final Class<?> parent, final double lowerRange,
      final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, parent, null, lowerRange, inclusive, Double.POSITIVE_INFINITY, false, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be greater than the lowest value provided.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param parent  the class where the methods are declared, not null
   * @param lowerRange  the lower range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsLowerRange(final Class<?> clazz, final Class<?> parent, final Object[] constructorArguments,
      final double lowerRange, final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, parent, constructorArguments, lowerRange, inclusive, Double.POSITIVE_INFINITY, false, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be less than the highest value provided.
   * @param clazz  the builder to be tested, not null
   * @param upperRange  the upper range
   * @param inclusive  true if the value can be equal to the upper range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsUpperRange(final Class<?> clazz, final double upperRange, final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, null, Double.NEGATIVE_INFINITY, false, upperRange, inclusive, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be less than the highest value provided.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param upperRange  the upper range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsUpperRange(final Class<?> clazz, final Object[] constructorArguments, final double upperRange,
      final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, constructorArguments, Double.NEGATIVE_INFINITY, false, upperRange, inclusive, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be less than the highest value provided.
   * @param clazz  the builder to be tested, not null
   * @param parent  the class where the methods are declared, not null
   * @param upperRange  the upper range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsUpperRange(final Class<?> clazz, final Class<?> parent, final double upperRange, final boolean inclusive,
      final String... methodNames) {
    testBuilderMethodsInputRange(clazz, parent, null, Double.NEGATIVE_INFINITY, false, upperRange, inclusive, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be less than the highest value provided.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param parent  the class where the methods are declared, not null
   * @param upperRange  the upper range
   * @param inclusive  true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsUpperRange(final Class<?> clazz, final Class<?> parent, final Object[] constructorArguments,
      final double upperRange, final boolean inclusive, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, parent, constructorArguments, Double.NEGATIVE_INFINITY, false, upperRange, inclusive, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be in the range provided.
   * @param clazz  the builder to be tested, not null
   * @param lowerRange  the lower range
   * @param upperRange  the upper range
   * @param inclusiveLower  true if the value can be equal to the lower range
   * @param inclusiveUpper true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsInputRange(final Class<?> clazz, final double lowerRange, final boolean inclusiveLower, final double upperRange,
      final boolean inclusiveUpper, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, null, lowerRange, inclusiveLower, upperRange, inclusiveUpper, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be in the range provided.
   * @param clazz  the builder to be tested, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param lowerRange  the lower range
   * @param upperRange  the upper range
   * @param inclusiveLower  true if the value can be equal to the lower range
   * @param inclusiveUpper true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsInputRange(final Class<?> clazz, final Object[] constructorArguments, final double lowerRange,
      final boolean inclusiveLower, final double upperRange, final boolean inclusiveUpper, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, constructorArguments, lowerRange, inclusiveLower, upperRange, inclusiveUpper, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be in the range provided.
   * @param clazz  the builder to be tested, not null
   * @param parent  the class where the methods are declared, not null
   * @param lowerRange  the lower range
   * @param upperRange  the upper range
   * @param inclusiveLower  true if the value can be equal to the lower range
   * @param inclusiveUpper true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsInputRange(final Class<?> clazz, final Class<?> parent, final double lowerRange, final boolean inclusiveLower,
      final double upperRange, final boolean inclusiveUpper, final String... methodNames) {
    testBuilderMethodsInputRange(clazz, clazz, null, lowerRange, inclusiveLower, upperRange, inclusiveUpper, methodNames);
  }

  /**
   * Tests that a numerical input to the methods must be in the range provided.
   * @param clazz  the builder to be tested, not null
   * @param parent  the class where the methods are declared, not null
   * @param constructorArguments  constructor arguments for the builder, null or empty means the default constructor is used
   * @param lowerRange  the lower range
   * @param upperRange  the upper range
   * @param inclusiveLower  true if the value can be equal to the lower range
   * @param inclusiveUpper true if the value can be equal to the lower range
   * @param methodNames  the method names, not null
   */
  public static void testBuilderMethodsInputRange(final Class<?> clazz, final Class<?> parent, final Object[] constructorArguments,
      final double lowerRange, final boolean inclusiveLower, final double upperRange, final boolean inclusiveUpper, final String... methodNames) {
    final Method[] methods = parent.getDeclaredMethods();
    for (final String methodName : methodNames) {
      boolean found = false;
      for (final Method method : methods) {
        if (!method.getName().equals(methodName)) {
          continue;
        }
        found = true;
        if (method.getReturnType().isAssignableFrom(clazz) && method.getParameterTypes().length == 1) {
          final Object builder = createBuilder(clazz, constructorArguments);
          if (builder == null) {
            fail("An instance of " + clazz.getSimpleName() + " could not be created with the arguments " + Arrays.toString(constructorArguments));
          }
          final Object[] args = new Object[1];
          // lower range
          if (inclusiveLower) {
            args[0] = lowerRange;
            try {
              method.invoke(builder, args);
              fail("Method called " + method.getName() + " should check that the input is not in the range "
                  + lowerRange + (inclusiveLower ? " <= " : " < ") + "x" + (inclusiveUpper ? " <= " : " < ") + upperRange);
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
      if (!found) {
        fail("Method called " + methodName + " not found in " + clazz.getSimpleName());
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
