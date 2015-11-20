/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Base class for classes that convert {@link CurveSpecificationBuilderConfiguration} to {@link CurveNodeIdMapper}. These classes
 * copy the data for each strip type into the appropriate methods in the node mapper.
 * <p>
 * This class provides renaming functions for a given method that returns the map from {@link Tenor} to {@link CurveInstrumentProvider} in the
 * {@link CurveSpecificationBuilderConfiguration} for a particular {@link com.opengamma.financial.analytics.ircurve.StripInstrumentType} e.g.
 * {@link CurveSpecificationBuilderConfiguration#getCashInstrumentProviders()}.
 * <p>
 * If the curve instrument providers were null or could not be identified, an empty {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}
 * is created. Otherwise, the appropriate curve instrument providers in the builder are populated..
 * <p>
 * This class uses reflection to call the correct method, but implementing classes should use a direct method call if they are intended to be
 * used for a single strip type.
 */
public abstract class InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentProviderPopulator.class);
  /** The renaming function */
  private final Function2<String, String, String> _renamingFunction;
  /** The strip instrument type */
  private final StripInstrumentType _type;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public InstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * @param renamingFunction  the renaming function, not null
   */
  public InstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.isTrue(isValidStripInstrumentType(type), "Strips of type {} are not valid for {}", type, getClass().getSimpleName());
    ArgumentChecker.notNull(renamingFunction, "renamingFunction");
    _type = type;
    //TODO test here that the method name exists?
    _renamingFunction = renamingFunction;
  }

  /**
   * Returns a (name, {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder})
   * pair with the id mapper populated with the appropriate (Tenor, CurveInstrumentProvider) map,
   * depending on the method called in {@link #getInstrumentProviders(CurveSpecificationBuilderConfiguration)}.
   * @param idMapper  the id mapper of which to create a copy, not null
   * @param identifiers  the curve specification builder configuration from which to copy the map, not null
   * @param currency  the currency of the curve specification builder configuration, not null
   * @return  a (id mapper name, builder) pair.
   */
  public Pair<String, CurveNodeIdMapper.Builder> apply(final CurveNodeIdMapper idMapper, final CurveSpecificationBuilderConfiguration identifiers,
      final String currency) {
    ArgumentChecker.notNull(idMapper, "idMapper");
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(currency, "currency");
    final Map<Tenor, CurveInstrumentProvider> instrumentProviders = getInstrumentProviders(identifiers);
    final String mapperName = rename(idMapper.getName(), currency);
    if (instrumentProviders == null || instrumentProviders.isEmpty()) {
      return Pairs.of(mapperName, copyToBuilder(idMapper, mapperName));
    }
    return Pairs.of(mapperName, createMapper(idMapper, instrumentProviders, mapperName));
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the appropriate map for the strip instrument type.
   * @param idMapper  the id mapper, not null
   * @param instrumentProviders  the instrument provider map, not null
   * @param mapperName  the new name for the mapper, not null
   * @return  a curve node id mapper builder with populated instrument provider maps.
   */
  private CurveNodeIdMapper.Builder createMapper(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    if (areNodesPopulated(idMapper)) {
      LOGGER.warn("Nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
    }
    return populateNodeIds(copyToBuilder(idMapper, mapperName), instrumentProviders);
  }

  /**
   * Populates the appropriate map for the strip instrument type. Note that the builder that is passed in is
   * altered in place.
   * @param idMapper  the id mapper, not null
   * @param instrumentProviders  the instrument provider map, not null
   * @return  a curve node id mapper builder with populated instrument provider maps.
   */
  protected abstract CurveNodeIdMapper.Builder populateNodeIds(CurveNodeIdMapper.Builder idMapper, Map<Tenor, CurveInstrumentProvider> instrumentProviders);

  protected abstract boolean isValidStripInstrumentType(StripInstrumentType type);

  protected abstract boolean areNodesPopulated(CurveNodeIdMapper idMapper);

  /**
   * Gets the map of instrument providers from the curve specification builder configuration.
   * @param csbc  the curve specification builder configuration, not null
   * @return  a map from tenor to curve instrument provider.
   */
  protected abstract Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc);

  /**
   * Applies a renaming function which generates a name for the curve node id mapper from
   * the name of the curve specification builder configuration and optionally the currency.
   * @param name  the name of the curve specification builder configuration, not null
   * @param currency  the currency string, not null
   * @return  the name of the curve node id mapper
   */
  private String rename(final String name, final String currency) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currency, "currency");
    return _renamingFunction.apply(name, currency);
  }

  /**
   * Creates a curve node id mapper builder from an original id mapper. The original id mapper
   * is not changed, and maps in the builder are copies of those in the original.
   * <p>
   * This method copies cash, continuously compounded rate, FRA, periodically compounded rate,
   * rate future and swap node maps. Other curve node types are not copied, as there is no
   * equivalent strip instrument type.
   * @param mapper  the mapper, not null
   * @param mapperName  the new name for the mapper, not null
   * @return  a builder with any non-null maps from the original mapper populated.
   */
  private static CurveNodeIdMapper.Builder copyToBuilder(final CurveNodeIdMapper mapper, final String mapperName) {
    ArgumentChecker.notNull(mapper, "mapper");
    ArgumentChecker.notNull(mapper, "mapperName");
    final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(mapperName);
    if (mapper.getAllTenors().size() == 0) {
      return builder;
    }
    if (mapper.getCashNodeIds() != null) {
      builder.cashNodeIds(new HashMap<>(mapper.getCashNodeIds()));
    }
    if (mapper.getContinuouslyCompoundedRateNodeIds() != null) {
      builder.continuouslyCompoundedRateNodeIds(new HashMap<>(mapper.getContinuouslyCompoundedRateNodeIds()));
    }
    if (mapper.getFRANodeIds() != null) {
      builder.fraNodeIds(new HashMap<>(mapper.getFRANodeIds()));
    }
    if (mapper.getPeriodicallyCompoundedRateNodeIds() != null) {
      builder.periodicallyCompoundedRateNodeIds(new HashMap<>(mapper.getPeriodicallyCompoundedRateNodeIds()));
    }
    if (mapper.getRateFutureNodeIds() != null) {
      builder.rateFutureNodeIds(new HashMap<>(mapper.getRateFutureNodeIds()));
    }
    if (mapper.getSwapNodeIds() != null) {
      builder.swapNodeIds(new HashMap<>(mapper.getSwapNodeIds()));
    }
    return builder;
  }

  protected StripInstrumentType getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_renamingFunction);
    result = prime * result + Objects.hashCode(_type);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InstrumentProviderPopulator)) {
      return false;
    }
    final InstrumentProviderPopulator other = (InstrumentProviderPopulator) obj;
    if (!Objects.equals(_type, other._type)) {
      return false;
    }
    if (!Objects.equals(_renamingFunction, other._renamingFunction)) {
      return false;
    }
    return true;
  }

}
