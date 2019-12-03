/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;

/**
 * Class to copy all HTS from one master to another.
 */
public class HistoricalTimeSeriesMasterCopier {
  private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalTimeSeriesMasterCopier.class);

  private final HistoricalTimeSeriesMaster _sourceMaster;
  private final HistoricalTimeSeriesMaster _destinationMaster;

  public HistoricalTimeSeriesMasterCopier(final HistoricalTimeSeriesMaster sourceMaster, final HistoricalTimeSeriesMaster destinationMaster) {
    _sourceMaster = sourceMaster;
    _destinationMaster = destinationMaster;
  }

  public void copy(final boolean fastCopy, final boolean deleteDestinationSeriesNotInSource, final boolean verbose, final boolean noAdditions) {
    final HistoricalTimeSeriesInfoSearchRequest infoSearchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchResult sourceSearchResult = _sourceMaster.search(infoSearchRequest);
    final List<ManageableHistoricalTimeSeriesInfo> sourceInfoList = sourceSearchResult.getInfoList();
    final HistoricalTimeSeriesInfoSearchResult destSearchResult = _destinationMaster.search(infoSearchRequest);
    final List<ManageableHistoricalTimeSeriesInfo> destInfoList = destSearchResult.getInfoList();
    final Set<ManageableHistoricalTimeSeriesInfo> bothInfoSetSource = new TreeSet<>(new ManageableHistoricalTimeSeriesInfoComparator());
    bothInfoSetSource.addAll(sourceInfoList);
    bothInfoSetSource.retainAll(destInfoList);
    final Set<ManageableHistoricalTimeSeriesInfo> bothInfoSetDestination = new TreeSet<>(new ManageableHistoricalTimeSeriesInfoComparator());
    bothInfoSetDestination.addAll(destInfoList);
    bothInfoSetDestination.retainAll(sourceInfoList);
    final Map<ManageableHistoricalTimeSeriesInfo, UniqueId> infoToSourceUniqueIds = Maps.newHashMap();
    // we have have two sets containing the TS in both, with the ids from the source in one and the ids from the dest in the other
    // now build a map of info->source uid
    for (final ManageableHistoricalTimeSeriesInfo info : bothInfoSetSource) {
      infoToSourceUniqueIds.put(info, info.getUniqueId());
    }
    // step through the destination results and look up the corresponding source id.
    for (final ManageableHistoricalTimeSeriesInfo info : bothInfoSetDestination) {
      if (infoToSourceUniqueIds.containsKey(info)) {
        if (verbose) {
          System.out.println("Time series " + info + " is in source and destination");
        }
        final UniqueId sourceId = infoToSourceUniqueIds.get(info);
        final UniqueId destinationId = info.getUniqueId();
        diffAndCopy(sourceId, destinationId, fastCopy, verbose);
      } else {
        throw new OpenGammaRuntimeException("Couldn't find info in set, which is supposed to be impossible");
      }
    }
    if (!noAdditions) {
      final Set<ManageableHistoricalTimeSeriesInfo> sourceNotDestinationInfo = new TreeSet<>(new ManageableHistoricalTimeSeriesInfoComparator());
      sourceNotDestinationInfo.addAll(sourceInfoList);
      sourceNotDestinationInfo.removeAll(destInfoList);
      for (final ManageableHistoricalTimeSeriesInfo info : sourceNotDestinationInfo) {
        if (verbose) {
          System.out.println("Time series " + info + " is in source but not destination");
        }
        add(info, verbose);
      }
    }
    if (deleteDestinationSeriesNotInSource) {
      final Set<ManageableHistoricalTimeSeriesInfo> destinationNotSourceInfo = new TreeSet<>(new ManageableHistoricalTimeSeriesInfoComparator());
      destinationNotSourceInfo.addAll(destInfoList);
      destinationNotSourceInfo.removeAll(sourceInfoList);
      for (final ManageableHistoricalTimeSeriesInfo info : destinationNotSourceInfo) {
        delete(info);
        if (verbose) {
          System.out.println("Deleted time series " + info + " which is in destination but not source");
        }
      }
    }
  }

  private void delete(final ManageableHistoricalTimeSeriesInfo info) {
    _destinationMaster.remove(info.getUniqueId());

  }

  private void add(final ManageableHistoricalTimeSeriesInfo sourceInfo, final boolean verbose) {
    final HistoricalTimeSeriesMasterUtils destinationMasterUtils = new HistoricalTimeSeriesMasterUtils(_destinationMaster);
    final HistoricalTimeSeries series  = _destinationMaster.getTimeSeries(sourceInfo.getUniqueId());
    destinationMasterUtils.writeTimeSeries(sourceInfo.getName(),
        sourceInfo.getDataSource(),
        sourceInfo.getDataProvider(),
        sourceInfo.getDataField(),
        sourceInfo.getObservationTime(),
        sourceInfo.getExternalIdBundle().toBundle(),
        series.getTimeSeries());
    if (verbose) {
      System.out.println("Added new time series to destination with " + series.getTimeSeries().size() + " data points");
    }
  }

  private boolean diffAndCopy(final UniqueId sourceId, final UniqueId destinationId, final boolean fastCopy, final boolean verbose) {
    if (fastCopy) {
      ManageableHistoricalTimeSeries sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId, HistoricalTimeSeriesGetFilter.ofLatestPoint());
      final ManageableHistoricalTimeSeries destTimeSeries = _destinationMaster.getTimeSeries(destinationId, HistoricalTimeSeriesGetFilter.ofLatestPoint());
      if (!sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries())) {
        final HistoricalTimeSeriesGetFilter filter = new HistoricalTimeSeriesGetFilter();
        final LocalDate lastSourceDate = sourceTimeSeries.getTimeSeries().getLatestTime();
        final LocalDate lastDestinationDate = destTimeSeries.getTimeSeries().getLatestTime();
        if (lastSourceDate.isAfter(lastDestinationDate)) {
          filter.setEarliestDate(lastDestinationDate.plusDays(1));
          filter.setLatestDate(lastSourceDate);
          sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId, filter);
          // get JUST the new days
          _destinationMaster.updateTimeSeriesDataPoints(destinationId, sourceTimeSeries.getTimeSeries());
          if (verbose) {
            System.out.println("Fast updating " + sourceTimeSeries.getTimeSeries().size() + " data points");
          }
          return true;
        }
        LOGGER.warn("Destination for " + destinationId + " has more up to date data than source, skipping!");
        return false;
      }
      if (verbose) {
        System.out.println("Fast compare of source and destination show they are the same, skipping");
      }
      return false;
    }
    ManageableHistoricalTimeSeries sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId);
    final ManageableHistoricalTimeSeries destTimeSeries = _destinationMaster.getTimeSeries(destinationId);
    if (!sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries())) {
      sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId);
      final HistoricalTimeSeriesMasterUtils masterUtils = new HistoricalTimeSeriesMasterUtils(_destinationMaster);
      masterUtils.writeTimeSeries(destinationId, sourceTimeSeries.getTimeSeries());
      if (verbose) {
        System.out.println("Full (slow) copy of source data to destination");
      } else {
        System.out.println("Full (slow) compare of source and destination show they are the same, skipping");
      }
    }
    return sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries());
  }


}

