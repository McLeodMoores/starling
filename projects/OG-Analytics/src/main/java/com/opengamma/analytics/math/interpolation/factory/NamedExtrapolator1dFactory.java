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
 * A factory for named instances of {@link NamedInterpolator} that perform extrapolation. The classes must have a
 * no-argument constructor to be created by this factory. In the cases where the extrapolator is not a named extrapolator,
 * this factory returns instances of {@link Extrapolator1dAdapter}, which wraps the original extrapolator and implements
 * {@link com.opengamma.util.NamedInstance}.
 */
@SuppressWarnings("rawtypes")
public final class NamedExtrapolator1dFactory extends AbstractNamedInstanceFactory<NamedExtrapolator> {
  /**
   * An instance of this factory.
   */
  public static final NamedExtrapolator1dFactory INSTANCE = new NamedExtrapolator1dFactory();

  /**
   * Gets the extrapolator with this name.
   * @param name The name, not null
   * @return  The extrapolator
   */
  @FromString
  public static NamedExtrapolator of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  private NamedExtrapolator1dFactory() {
    super(NamedExtrapolator.class);
    loadFromProperties();
  }

  @Override
  protected void loadFromProperties(final String bundleName) {
    ArgumentChecker.notNull(bundleName, "bundleName");
    final ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    final Map<String, NamedExtrapolator<?, ?>> instances = Maps.newHashMap();
    for (final String name : bundle.keySet()) {
      final String implementationType = bundle.getString(name);
      NamedExtrapolator<?, ?> instance = instances.get(implementationType);
      if (instance == null) {
        try {
          @SuppressWarnings("unchecked")
          final Class<? extends Interpolator<?, ?>> clazz =
            (Class<? extends Interpolator<?, ?>>) ClassUtils.loadClassRuntime(implementationType).asSubclass(Interpolator.class);
          if (NamedExtrapolator.class.isAssignableFrom(clazz)) {
            instance = (NamedExtrapolator<?, ?>) clazz.newInstance();
          } else {
            final Constructor<?>[] constructors = clazz.getConstructors();
            for (final Constructor<?> constructor : constructors) {
              final Class<?>[] parameterTypes = constructor.getParameterTypes();
              if (parameterTypes.length == 0) {
                instance = new Extrapolator1dAdapter((Interpolator1D) constructor.newInstance((Object[]) null), name);
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
