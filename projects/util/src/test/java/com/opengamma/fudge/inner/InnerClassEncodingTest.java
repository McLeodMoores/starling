/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.fudgemsg.AutoFudgable.autoFudge;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class InnerClassEncodingTest extends AbstractFudgeBuilderTestCase {

  Random generator = new Random(System.currentTimeMillis());

  public void test_inner_without_context() {
    final TestOuterClass inner = new TestOuterClass() {
    };



    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_primitive_context() {
    final double someContext = generator.nextDouble();
    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        return arg * someContext;
      }
    };



    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_two_primitive_contexts() {
    final double someContextA = 1.0;
    final double someContextB = 2.0;
    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        return arg * someContextA + someContextB;
      }
    };

    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_array_of_primitives_context() {

    final int count = generator.nextInt(100);
    final double[] someContext = new double[count];

    for (int j = 0; j < count; j++) {
      someContext[j] = generator.nextDouble();
    }

    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final double d : someContext) {
          sum += d;
        }
        return sum;
      }
    };

    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_pojo_context() {
    final ContextPOJO someContext = new ContextPOJO();
    someContext.setValue(generator.nextDouble());

    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        return arg * someContext.getValue();
      }
    };

    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  /**
   * This fails because fudge can't serialize arrays of something other than primitives
   */
  @Test(enabled = false)
  public void test_inner_with_array_of_pojos_context() {
    final int count = generator.nextInt(100);
    final ContextPOJO[] someContext = new ContextPOJO[count];

    for (int j = 0; j < count; j++) {
      someContext[j] = new ContextPOJO();
      someContext[j].setValue(generator.nextDouble());
    }

    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final ContextPOJO pojo : someContext) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };



    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_list_of_pojos_context() {
    final int count = generator.nextInt(100);
    final List<ContextPOJO> someContext = newArrayList();

    for (int j = 0; j < count; j++) {
      final ContextPOJO pojo = new ContextPOJO();
      pojo.setValue(generator.nextDouble());
      someContext.add(pojo);
    }

    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final ContextPOJO pojo : someContext) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };



    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_context_copied_from_enclosing_class() {
    final double someContext = some_outer_context;
    final TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(final double arg) {
        return arg * someContext;
      }
    };



    final TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void test_inner_implementing_iface_without_context() {
    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        return arg;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_primitive_context() {
    final double someContext = generator.nextDouble();
    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        return arg * someContext;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }


  public void test_inner_implementing_iface_with_array_of_primitives_context() {

    final int count = generator.nextInt(100);
    final double[] someContext = new double[count];

    for (int j = 0; j < count; j++) {
      someContext[j] = generator.nextDouble();
    }

    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final double d : someContext) {
          sum += d;
        }
        return sum;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_pojo_context() {
    final ContextPOJO someContext = new ContextPOJO();
    someContext.setValue(generator.nextDouble());

    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        return arg * someContext.getValue();
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  /**
   * This fails because fudge can't serialize arrays of something other than primitives
   */
  @Test(enabled = false)
  public void test_inner_implementing_iface_with_array_of_pojos_context() {
    final int count = generator.nextInt(100);
    final ContextPOJO[] someContext = new ContextPOJO[count];

    for (int j = 0; j < count; j++) {
      someContext[j] = new ContextPOJO();
      someContext[j].setValue(generator.nextDouble());
    }

    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final ContextPOJO pojo : someContext) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_list_of_pojos_context() {
    final int count = generator.nextInt(100);
    final List<ContextPOJO> someContext = newArrayList();

    for (int j = 0; j < count; j++) {
      final ContextPOJO pojo = new ContextPOJO();
      pojo.setValue(generator.nextDouble());
      someContext.add(pojo);
    }

    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        double sum = arg;
        for (final ContextPOJO pojo : someContext) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  double some_outer_context = generator.nextDouble();

  public void test_inner_implementing_iface_with_context_copied_from_enclosing_class() {
    final double someContext = some_outer_context;
    final TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(final double arg) {
        return arg * someContext;
      }
    };

    final TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      final double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_a_collection_which_is_inner_class() {
    final Map<Byte, Byte> map = Collections.unmodifiableMap(new HashMap<Byte, Byte>() {
      private static final long serialVersionUID = 1L;
      {
      this.put((byte) 1, (byte) 2);
      }
    });
    @SuppressWarnings("rawtypes")
    final
    Map cycled = cycleObjectOverBytes(map);

    assertEquals(cycled.get((byte) 1), (byte) 2);
  }

}
