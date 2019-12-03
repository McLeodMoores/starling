/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.component;

import java.util.LinkedHashMap;

import org.springframework.context.support.GenericApplicationContext;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractSpringComponentFactory;

/**
 * Spring-based data server.
 */
public class ExampleSpringDataServerComponentFactory extends AbstractSpringComponentFactory {

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    final GenericApplicationContext appContext = createApplicationContext(repo);
    repo.registerLifecycle(appContext);
  }

}
