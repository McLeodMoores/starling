/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * Utility class to provide services to snapshot command line tools (and potentially UI tools too).
 */
public final class SnapshotUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotUtils.class);

  private final MarketDataSnapshotMaster _snapshotMaster;

  private SnapshotUtils(final MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
  }

  public static SnapshotUtils of(final MarketDataSnapshotMaster snapshotMaster) {
    return new SnapshotUtils(snapshotMaster);
  }

  private static String getSnapshotNameId(final MarketDataSnapshotDocument doc) {
    return doc.getUniqueId() + " - " + doc.getName();
  }

  /**
   * Get a list of all available snapshots
   * @return the list of all available snapshot ids and names or an empty list if no snapshots found
   */
  public List<String> allSnapshots() {
    final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    final List<String> results = new ArrayList<>();
    for (final MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      results.add(getSnapshotNameId(doc));
    }
    return results;
  }

  /**
   * Get a list of snapshot according to a glob query string
   * @param query the query string, which can contain wildcards
   * @return the list of resulting snapshot ids and names or an empty list if no matches
   */
  public List<String> snapshotByGlob(final String query) {
    final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(query);
    searchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    final List<String> results = new ArrayList<>();
    for (final MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      results.add(getSnapshotNameId(doc));
    }
    return results;
  }

  /**
   * Get the latest snapshot by name
   * @param name exact name of the snapshot, not null
   * @return the UniqueId of the matched snapshot, or null if no match found
   * @throws OpenGammaRuntimeException if multiple matches are found
   */
  public UniqueId latestSnapshotByName(final String name) {
    final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    if (searchResult.getDocuments().size() > 1) {
      throw new OpenGammaRuntimeException("More than one snapshot matches supplied name");
    }
    if (searchResult.getDocuments().size() == 0) {
      return null;
    }
    return searchResult.getFirstDocument().getUniqueId();
  }

  /**
   * Get the latest snapshot by name
   * @param name exact name of the snapshot, not null
   * @param dateTime the date/time of the version of the snapshot to fetch
   * @return the UniqueId of the matched snapshot, or null if no match found
   * @throws OpenGammaRuntimeException if multiple matches are found
   */
  public UniqueId latestSnapshotByNameAndDate(final String name, final ZonedDateTime dateTime) {
    final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    searchRequest.setVersionCorrection(VersionCorrection.ofVersionAsOf(dateTime.toInstant()));
    if (searchResult.getDocuments().size() > 1) {
      throw new OpenGammaRuntimeException("More than one snapshot matches supplied name");
    }
    if (searchResult.getDocuments().size() == 0) {
      return null;
    }
    return searchResult.getFirstDocument().getUniqueId();
  }

  /**
   * Get meta data about available versions of a snapshot by it's name
   * @param name exact name of the snapshot, not null
   * @return a list of VersionInfo meta data objects containing version correction ranges and unique ids
   * @throws OpenGammaRuntimeException if multiple name matches are found
   */
  public List<VersionInfo> snapshotVersionsByName(final String name) {
    final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(name);
    searchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult searchResult = _snapshotMaster.search(searchRequest);
    if (searchResult.getDocuments().size() > 1) {
      LOGGER.warn("More than one snapshot matches supplied name, using first");
    }
    if (searchResult.getDocuments().size() == 0) {
      return Collections.emptyList();
    }
    final ObjectId objectId = searchResult.getFirstDocument().getObjectId();
    final MarketDataSnapshotHistoryResult historyResult = _snapshotMaster.history(new MarketDataSnapshotHistoryRequest(objectId));
    final List<VersionInfo> results = new ArrayList<>();
    for (final MarketDataSnapshotDocument doc : historyResult.getDocuments()) {
      results.add(new VersionInfo(doc.getVersionFromInstant(), doc.getCorrectionFromInstant(), doc.getVersionToInstant(), doc.getCorrectionToInstant(), doc.getUniqueId()));
    }
    return results;
  }

  /**
   * Class representing the version range information for a snapshot, including the UniqueId.
   */
  public class VersionInfo {
    private final Instant _versionFrom;
    private final Instant _versionTo;
    private final Instant _correctionFrom;
    private final Instant _correctionTo;
    private final UniqueId _uniqueId;

    public VersionInfo(final Instant versionFrom, final Instant versionTo, final Instant correctionFrom, final Instant correctionTo, final UniqueId uniqueId) {
      _versionFrom = versionFrom;
      _versionTo = versionTo;
      _correctionFrom = correctionFrom;
      _correctionTo = correctionTo;
      _uniqueId = uniqueId;
    }

    public Instant getVersionFrom() {
      return _versionFrom;
    }

    public Instant getVersionTo() {
      return _versionTo;
    }

    public Instant getCorrectionFrom() {
      return _correctionFrom;
    }

    public Instant getCorrectionTo() {
      return _correctionTo;
    }

    public UniqueId getUniqueId() {
      return _uniqueId;
    }

  }

}
