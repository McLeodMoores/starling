/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.convert.FromString;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;

/**
 * A factory for named instances of {@link ExposureFunction}. The classes must have a no-argument constructor
 * to be created by this factory. In the cases where the exposure function is not a named exposure function,
 * this factory returns instances of {@link ExposureFunctionAdapter}, which wraps the original exposure function
 * and implements {@link com.opengamma.util.NamedInstance}.
 * <p>
 * This class currently loads the instances from a properties file.
 */
public final class NamedExposureFunctionFactory extends AbstractNamedInstanceFactory<NamedExposureFunction> {
  /**
   * An instance of this factory.
   */
  public static final NamedExposureFunctionFactory INSTANCE = new NamedExposureFunctionFactory();

  /**
   * Gets the exposure function with this name.
   * @param name The name, not null
   * @return The exposure function
   */
  @FromString
  public static NamedExposureFunction of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  private NamedExposureFunctionFactory() {
    super(NamedExposureFunction.class);
    loadFromProperties();
  }

  @Override
  protected void loadFromProperties(final String bundleName) {
    ArgumentChecker.notNull(bundleName, "bundleName");
    final ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    final Map<String, NamedExposureFunction> instances = Maps.newHashMap();
    for (final String name : bundle.keySet()) {
      final String implementationType = bundle.getString(name);
      NamedExposureFunction instance = instances.get(implementationType);
      if (instance == null) {
        try {
          final Class<? extends ExposureFunction> clazz = ClassUtils.loadClassRuntime(implementationType).asSubclass(ExposureFunction.class);
          if (NamedExposureFunction.class.isAssignableFrom(clazz)) {
            instance = (NamedExposureFunction) clazz.newInstance();
          } else {
            final Constructor<?>[] constructors = clazz.getConstructors();
            for (final Constructor<?> constructor : constructors) {
              final Class<?>[] parameterTypes = constructor.getParameterTypes();
              if (parameterTypes.length == 0) {
                instance = new ExposureFunctionAdapter((ExposureFunction) constructor.newInstance((Object[]) null));
              } else if (parameterTypes.length == 1 && parameterTypes[0].equals(SecuritySource.class)) {
                instance = new ExposureFunctionAdapter((ExposureFunction) constructor.newInstance(DummySecuritySource.getInstance()));
              }
            }
          }
          instances.put(implementationType, instance);
        } catch (final Exception ex) {
          throw new OpenGammaRuntimeException("Error loading properties for " + implementationType, ex);
        }
      }
      addInstance(instance, name);
    }
  }
}
