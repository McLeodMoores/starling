/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;

/**
 * Jersey specific class to integrate per-request resources into Jersey JaxRs.
 */
public final class JerseyRestResourceFactory extends RestResourceFactory implements IoCComponentProviderFactory, IoCManagedComponentProvider {

  /**
   * Creates an instance of the factory.
   *
   * @param type  the type to create, not null
   * @param arguments  the arguments
   */
  public JerseyRestResourceFactory(final Class<?> type, final Object... arguments) {
    super(type, arguments);
  }

  //-------------------------------------------------------------------------
  @Override
  public IoCComponentProvider getComponentProvider(final Class<?> clazz) {
    return getComponentProvider(null, clazz);
  }

  @Override
  public IoCComponentProvider getComponentProvider(final ComponentContext cc, final Class<?> clazz) {
    if (clazz.equals(getType())) {
      return this;
    }
    return null;
  }

  //-------------------------------------------------------------------------
  @Override
  public Object getInstance() {
    return createInstance();
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override
  public Object getInjectableInstance(final Object o) {
    return o;
  }

}
