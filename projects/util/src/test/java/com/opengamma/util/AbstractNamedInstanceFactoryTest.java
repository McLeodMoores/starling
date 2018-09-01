/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util;

/**
 *
 */
public class AbstractNamedInstanceFactoryTest {

  public static class Test1 implements NamedInstance {

    @Override
    public String getName() {
      return "TEST1";
    }

  }

  public static class Test2 implements NamedInstance {

    @Override
    public String getName() {
      return "TEST2";
    }
  }

  private class TestNamedInstanceFactory extends AbstractNamedInstanceFactory<NamedInstance> {

    protected TestNamedInstanceFactory() {
      super(NamedInstance.class);
    }

  }
}
