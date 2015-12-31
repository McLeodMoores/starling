/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.testutils;

import java.util.LinkedList;
import java.util.List;

import org.joda.beans.BeanDefinition;

import com.opengamma.component.factory.source.FunctionConfigurationSourceComponentFactory;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 *
 */
@BeanDefinition
public class EmptyFunctionConfigurationSourceComponentFactory extends FunctionConfigurationSourceComponentFactory {

  @Override
  protected List<FunctionConfigurationSource> initSources() {
    return new LinkedList<>();
  }
}
