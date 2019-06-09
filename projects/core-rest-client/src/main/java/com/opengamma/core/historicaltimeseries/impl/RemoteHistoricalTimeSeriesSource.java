/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeMapWrapper;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Provides remote access to an {@link HistoricalTimeSeriesSource}.
 */
public class RemoteHistoricalTimeSeriesSource extends AbstractRemoteClient implements HistoricalTimeSeriesSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri
   *          the base target URI for all RESTful web services, not null
   */
  public RemoteHistoricalTimeSeriesSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri
   *          the base target URI for all RESTful web services, not null
   * @param changeManager
   *          the change manager, not null
   */
  public RemoteHistoricalTimeSeriesSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  // -------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriGet(getBaseUri(), uniqueId);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriGet(getBaseUri(), uniqueId, start, includeStart, end, includeEnd, null);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd, final int maxPoints) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriGet(getBaseUri(), uniqueId, start, includeStart, end, includeEnd, maxPoints);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  // -------------------------------------------------------------------------
  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final UniqueId uniqueId) {
    return extractPair(getHistoricalTimeSeries(uniqueId, null, true, null, true, -1));
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd) {
    return extractPair(getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, -1));
  }

  // -------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider,
      final String dataField) {
    return getHistoricalTimeSeries(identifierBundle, dataSource, dataProvider, dataField, null, true, null, true);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider,
      final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchSingle(
          getBaseUri(), identifierBundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, null);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider,
      final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchSingle(
          getBaseUri(), identifierBundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField) {
    return getHistoricalTimeSeries(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, null, true, null, true);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchSingle(
          getBaseUri(), identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, null);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchSingle(
          getBaseUri(), identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  // -------------------------------------------------------------------------
  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField) {
    return getLatestDataPoint(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, null, true, null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    return extractPair(
        getHistoricalTimeSeries(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, -1));
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider,
      final String dataField) {
    return getLatestDataPoint(identifierBundle, dataSource, dataProvider, dataField, null, true, null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider,
      final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    return extractPair(getHistoricalTimeSeries(identifierBundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, -1));
  }

  // -------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, null, true, null, true);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchResolve(
          getBaseUri(), identifierBundle, dataField, resolutionKey, start, includeStart, end, includeEnd, null);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchResolve(
          getBaseUri(), identifierBundle, dataField, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, null, true, null, true);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchResolve(
          getBaseUri(), identifierBundle, identifierValidityDate, dataField, resolutionKey, start, includeStart, end, includeEnd, null);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchResolve(
          getBaseUri(), identifierBundle, identifierValidityDate, dataField, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      return accessRemote(uri).get(HistoricalTimeSeries.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  // -------------------------------------------------------------------------
  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, resolutionKey, null, true, null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    return extractPair(getHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, start, includeStart, end, includeEnd, -1));
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, identifierValidityDate, resolutionKey, null, true, null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    return extractPair(getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, -1));
  }

  // -------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(final Set<ExternalIdBundle> identifierSet, final String dataSource,
      final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    ArgumentChecker.notNull(identifierSet, "identifierSet");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriSearchBulk(getBaseUri());
      final FudgeMsg msg = DataHistoricalTimeSeriesSourceUris.uriSearchBulkData(identifierSet, dataSource, dataProvider, dataField, start, includeStart, end,
          includeEnd);
      return accessRemote(uri).post(FudgeMapWrapper.class, msg).getMap();
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  // -------------------------------------------------------------------------
  private static Pair<LocalDate, Double> extractPair(final HistoricalTimeSeries historicalTimeSeries) {
    if (historicalTimeSeries == null) {
      return null;
    }
    final LocalDateDoubleTimeSeries series = historicalTimeSeries.getTimeSeries();
    if (series.size() == 0) {
      return null;
    }
    return Pairs.of(series.getLatestTime(), series.getLatestValue());
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      final URI uri = DataHistoricalTimeSeriesSourceUris.uriExternalIdBundleGet(getBaseUri(), uniqueId);
      return accessRemote(uri).get(ExternalIdBundle.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
