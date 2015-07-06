/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.convert.FromString;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.Interpolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;

/**
 * A factory for named instances of {@link NamedInterpolator}. The classes must have a no-argument constructor
 * to be created by this factory. In the cases where the interpolator is not a named interpolator, this factory
 * returns instances of {@link Interpolator1dAdapter}, which wraps the original interpolator and implements
 * {@link com.opengamma.util.NamedInstance}.
 */
@SuppressWarnings("rawtypes")
public final class NamedInterpolator1dFactory extends AbstractNamedInstanceFactory<NamedInterpolator> {
  /**
   * An instance of this factory.
   */
  public static final NamedInterpolator1dFactory INSTANCE = new NamedInterpolator1dFactory();

  /**
   * Gets the interpolator with this name.
   * @param name The name, not null
   * @return  The interpolator
   */
  @FromString
  public static NamedInterpolator of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  private NamedInterpolator1dFactory() {
    super(NamedInterpolator.class);
    loadFromProperties();
  }

  @Override
  protected void loadFromProperties(final String bundleName) {
    ArgumentChecker.notNull(bundleName, "bundleName");
    final ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    final Map<String, NamedInterpolator<?, ?>> instances = Maps.newHashMap();
    for (final String name : bundle.keySet()) {
      final String implementationType = bundle.getString(name);
      NamedInterpolator<?, ?> instance = instances.get(implementationType);
      if (instance == null) {
        try {
          @SuppressWarnings("unchecked")
          final Class<? extends Interpolator<?, ?>> clazz =
          (Class<? extends Interpolator<?, ?>>) ClassUtils.loadClassRuntime(implementationType).asSubclass(Interpolator.class);
          if (NamedInterpolator.class.isAssignableFrom(clazz)) {
            instance = (NamedInterpolator<?, ?>) clazz.newInstance();
          } else {
            final Constructor<?>[] constructors = clazz.getConstructors();
            for (final Constructor<?> constructor : constructors) {
              final Class<?>[] parameterTypes = constructor.getParameterTypes();
              if (parameterTypes.length == 0) {
                instance = new Interpolator1dAdapter((Interpolator1D) constructor.newInstance((Object[]) null), name);
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
