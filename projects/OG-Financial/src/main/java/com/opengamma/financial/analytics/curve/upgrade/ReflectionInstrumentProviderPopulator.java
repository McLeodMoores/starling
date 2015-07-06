/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ReflectionInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The name of the method in the curve specification builder configuration that supplies the appropriate ids for
   * the strip instrument type. */
  private final String _instrumentProviderMethodName;
  /** The name of the method in the curve node id mapper builder that adds ids to the appropriate curve node type */
  private final String _builderMethodName;
  /** The name of the method in the curve node id mapper builder that gets the ids for the appropriate curve node type */
  private final String _builderGetterName;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param type  the strip instrument type, not null
   * @param instrumentProviderName  the instrument provider method name, not null
   * @param builderGetterName  the name of the method in the {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}
   * that gets the ids for the curve node type, not null
   * @param builderMethodName  the name of the method in the {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}
   * that adds the ids for the curve not type, not null
   */
  public ReflectionInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName,
      final String builderGetterName, final String builderMethodName) {
    this(type, instrumentProviderName, builderGetterName, builderMethodName, new DefaultCsbcRenamingFunction());
  }

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param type  the strip instrument type, not null
   * @param instrumentProviderName  the instrument provider method name, not null
   * @param builderGetterName  the name of the method in the {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}
   * that gets the ids for the curve node type, not null
   * @param builderMethodName  the name of the method in the {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}
   * that adds the ids for the curve not type, not null
   * @param renamingFunction  the name of the renaming function, not null
   */
  public ReflectionInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName,
      final String builderGetterName, final String builderMethodName, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
    _instrumentProviderMethodName = instrumentProviderName;
    _builderGetterName = builderGetterName;
    _builderMethodName = builderMethodName;
  }

  @Override
  protected Builder populateNodeIds(final Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    try {
      final Method builderMethod = Builder.class.getMethod(_builderMethodName, Map.class);
      return (Builder) builderMethod.invoke(idMapper, instrumentProviders);
    } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return true;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    try {
      final Method getMethod = CurveNodeIdMapper.class.getMethod(_builderGetterName, (Class<?>[]) null);
      return getMethod.invoke(idMapper, (Object[]) null) != null;
    } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Gets the map of instrument providers from the curve specification builder configuration.
   * This method uses reflection to call the correct getter and can be overridden in implementing
   * classes that only handle one strip instrument type to improve performance. If the getter
   * cannot be found or there is a problem calling the getter a runtime exception will be thrown.
   * @param csbc  the curve specification builder configuration, not null
   * @return  a map from tenor to curve instrument provider.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    ArgumentChecker.notNull(csbc, "csbc");
    try {
      final Method method = csbc.getClass().getMethod(_instrumentProviderMethodName, (Class<?>[]) null);
      return (Map<Tenor, CurveInstrumentProvider>) method.invoke(csbc, (Object[]) null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}