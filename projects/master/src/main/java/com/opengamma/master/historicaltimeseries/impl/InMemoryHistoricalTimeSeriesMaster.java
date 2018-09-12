/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.DoubleTimeSeriesOperators;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a historical time-series master.
 */
public class InMemoryHistoricalTimeSeriesMaster
extends SimpleAbstractInMemoryMaster<HistoricalTimeSeriesInfoDocument>
implements HistoricalTimeSeriesMaster {

  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemHts";

  /**
   * A cache of time-series points by identifier.
   */
  private final ConcurrentMap<ObjectId, LocalDateDoubleTimeSeries> _storePoints = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   */
  public InMemoryHistoricalTimeSeriesMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryHistoricalTimeSeriesMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(final HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    if (document.getUniqueId() != null) {
      validateId(document.getUniqueId());
    }
    ArgumentChecker.notNull(document.getInfo(), "document.series");
    ArgumentChecker.notNull(document.getInfo().getExternalIdBundle(), "document.series.identifiers");
    ArgumentChecker.isTrue(document.getInfo().getExternalIdBundle().toBundle().getExternalIds().size() > 0, "document.series.identifiers must not be empty");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataSource()), "document.series.dataSource must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataProvider()), "document.series.dataProvider must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getDataField()), "document.series.dataField must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getInfo().getObservationTime()), "document.series.observationTime must not be blank");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(final HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    final HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    if (request.isDataFields()) {
      final Set<String> types = new HashSet<>();
      for (final HistoricalTimeSeriesInfoDocument doc : _store.values()) {
        types.add(doc.getInfo().getDataField());
      }
      result.getDataFields().addAll(types);
    }
    if (request.isDataSources()) {
      final Set<String> types = new HashSet<>();
      for (final HistoricalTimeSeriesInfoDocument doc : _store.values()) {
        types.add(doc.getInfo().getDataSource());
      }
      result.getDataSources().addAll(types);
    }
    if (request.isDataProviders()) {
      final Set<String> types = new HashSet<>();
      for (final HistoricalTimeSeriesInfoDocument doc : _store.values()) {
        types.add(doc.getInfo().getDataProvider());
      }
      result.getDataProviders().addAll(types);
    }
    if (request.isObservationTimes()) {
      final Set<String> types = new HashSet<>();
      for (final HistoricalTimeSeriesInfoDocument doc : _store.values()) {
        types.add(doc.getInfo().getObservationTime());
      }
      result.getObservationTimes().addAll(types);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<HistoricalTimeSeriesInfoDocument> list = new ArrayList<>();
    for (final HistoricalTimeSeriesInfoDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final ObjectIdentifiable objectKey, final VersionCorrection versionCorrection) {
    validateId(objectKey);
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ObjectId objectId = objectKey.getObjectId();
    final HistoricalTimeSeriesInfoDocument document = _store.get(objectId);
    if (document == null) {
      throw new DataNotFoundException("Historical time-series not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument add(final HistoricalTimeSeriesInfoDocument document) {
    validateDocument(document);

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final HistoricalTimeSeriesInfoDocument cloned = JodaBeanUtils.clone(document);
    final ManageableHistoricalTimeSeriesInfo info = cloned.getInfo();
    info.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    cloned.setVersionFromInstant(now);
    cloned.setCorrectionFromInstant(now);
    cloned.getInfo().setTimeSeriesObjectId(objectId);
    _store.put(objectId, cloned);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, cloned.getVersionFromInstant(), cloned.getVersionToInstant(), now);
    return cloned;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument update(final HistoricalTimeSeriesInfoDocument document) {
    validateDocument(document);
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final HistoricalTimeSeriesInfoDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (!_store.replace(uniqueId.getObjectId(), storedDocument, document)) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    validateId(objectIdentifiable);
    if (_store.remove(objectIdentifiable.getObjectId()) == null) {
      throw new DataNotFoundException("Historical time-series not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument correct(final HistoricalTimeSeriesInfoDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(final HistoricalTimeSeriesInfoHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final HistoricalTimeSeriesInfoHistoryResult result = new HistoricalTimeSeriesInfoHistoryResult();
    final HistoricalTimeSeriesInfoDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId) {
    return getTimeSeries(uniqueId.getObjectId(), VersionCorrection.LATEST);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId, final HistoricalTimeSeriesGetFilter filter) {
    return getTimeSeries(uniqueId.getObjectId(), VersionCorrection.LATEST, filter);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return getTimeSeries(objectId, versionCorrection, HistoricalTimeSeriesGetFilter.ofRange(null, null));
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectKey, final VersionCorrection versionCorrection,
      final HistoricalTimeSeriesGetFilter filter) {
    validateId(objectKey);
    final LocalDate fromDateInclusive = MoreObjects.firstNonNull(filter.getEarliestDate(), LocalDate.of(1000, 1, 1));  // TODO: JSR-310 min/max date
    final LocalDate toDateInclusive = MoreObjects.firstNonNull(filter.getLatestDate(), LocalDate.of(9999, 1, 1));
    //    final LocalDate fromDateInclusive = MoreObjects.firstNonNull(filter.getEarliestDate(), LocalDate.MIN);
    //    final LocalDate toDateInclusive = MoreObjects.firstNonNull(filter.getLatestDate(), LocalDate.MAX);
    ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
    final ObjectId objectId = objectKey.getObjectId();

    final Instant now = Instant.now();
    LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries == null) {
      if (_store.get(objectId) == null) {
        throw new DataNotFoundException("Historical time-series not found: " + objectId);
      }
      existingSeries = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
    }

    // Filter points by date range and max points to return
    // Heeds LocalDateDoubleTimeSeries convention: inclusive start, exclusive end
    LocalDateDoubleTimeSeries subSeries = existingSeries.subSeries(fromDateInclusive, toDateInclusive.plusDays(1));
    final Integer maxPoints = filter.getMaxPoints();
    if (maxPoints != null && Math.abs(maxPoints) < subSeries.size()) {
      subSeries = maxPoints >= 0 ? subSeries.head(maxPoints) : subSeries.tail(-maxPoints);
    }

    final ManageableHistoricalTimeSeries result = new ManageableHistoricalTimeSeries();
    result.setUniqueId(objectId.atLatestVersion());
    result.setTimeSeries(subSeries);
    result.setVersionInstant(now);
    result.setCorrectionInstant(now);
    return result;
  }


  //-------------------------------------------------------------------------
  @Override
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectKey, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    ArgumentChecker.notNull(series, "series");
    final ObjectId objectId = objectKey.getObjectId();

    final LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries != null) {
      if (existingSeries.size() > 0 && !(series.getLatestTime().isBefore(existingSeries.getEarliestTime())
          || series.getEarliestTime().isAfter(existingSeries.getLatestTime()))) {
        throw new IllegalArgumentException("Unable to add time-series as dates overlap");
      }
      final LocalDateDoubleTimeSeries newSeries = existingSeries.noIntersectionOperation(series);
      if (!_storePoints.replace(objectId, existingSeries, newSeries)) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    } else {
      if (_storePoints.putIfAbsent(objectId, series) != null) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    }
    final Instant now = Instant.now();
    final UniqueId uniqueId = objectId.atLatestVersion();
    changeManager().entityChanged(ChangeType.CHANGED, objectId, null, null, now);
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectKey, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    ArgumentChecker.notNull(series, "series");
    final ObjectId objectId = objectKey.getObjectId();

    final LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries != null) {
      final LocalDateDoubleTimeSeries newSeries = existingSeries.unionOperate(series, DoubleTimeSeriesOperators.SECOND_OPERATOR);
      if (!_storePoints.replace(objectId, existingSeries, newSeries)) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    } else {
      if (_storePoints.putIfAbsent(objectId, series) != null) {
        throw new IllegalArgumentException("Concurrent modification");
      }
    }
    final Instant now = Instant.now();
    final UniqueId uniqueId = objectId.atLatestVersion();
    changeManager().entityChanged(ChangeType.CHANGED, objectId, null, null, now);
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectKey, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectKey, "objectKey");
    final LocalDate fromDate = MoreObjects.firstNonNull(fromDateInclusive, LocalDate.MIN);
    final LocalDate toDate = MoreObjects.firstNonNull(toDateInclusive, LocalDate.MAX);
    ArgumentChecker.inOrderOrEqual(fromDate, toDate, "fromDate", "toDate");
    final ObjectId objectId = objectKey.getObjectId();

    final LocalDateDoubleTimeSeries existingSeries = _storePoints.get(objectId);
    if (existingSeries == null) {
      return objectId.atLatestVersion();
    }
    final LocalDateDoubleTimeSeriesBuilder bld = existingSeries.toBuilder();
    for (final LocalDateDoubleEntryIterator it = bld.iterator(); it.hasNext();) {
      final LocalDate date = it.nextTime();
      if (!date.isBefore(fromDate) && !date.isAfter(toDate)) {
        it.remove();
      }
    }
    if (!_storePoints.replace(objectId, existingSeries, bld.build())) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return objectId.atLatestVersion();
  }

  //-------------------------------------------------------------------------
  private static long validateId(final ObjectIdentifiable objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    try {
      return Long.parseLong(objectId.getObjectId().getValue());
    } catch (final NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid objectId " + objectId);
    }
  }

}
