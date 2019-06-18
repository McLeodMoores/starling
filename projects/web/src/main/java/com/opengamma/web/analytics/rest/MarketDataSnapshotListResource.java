/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource that produces a JSON list of market data snapshots and their IDs.  This isn't a full REST
 * interface for market data snapshots, it's intended for populating data in the web client.
 */
@Path("marketdatasnapshots")
public class MarketDataSnapshotListResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSnapshotListResource.class);

  static final String BASIS_VIEW_NAME = "basisViewName";
  static final String SNAPSHOTS = "snapshots";
  static final String ID = "id";
  static final String NAME = "name";

  private static final Pattern GUID_PATTERN =
      Pattern.compile("(\\{?([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}?)");

  private final MarketDataSnapshotMaster _snapshotMaster;

  public MarketDataSnapshotListResource(final MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _snapshotMaster = snapshotMaster;
  }

  /**
   * @return JSON {@code [{basisViewName: basisViewName1, snapshots: [{id: snapshot1Id, name: snapshot1Name}, ...]}, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getMarketDataSnapshotList() {
    final MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);

    final Multimap<String, ManageableMarketDataSnapshot> snapshotsByBasisView = LinkedListMultimap.create();
    for (final MarketDataSnapshotDocument doc : MarketDataSnapshotSearchIterator.iterable(_snapshotMaster, snapshotSearchRequest)) {
      final ManageableMarketDataSnapshot snapshot = (ManageableMarketDataSnapshot) doc.getNamedSnapshot();
      if (snapshot.getUniqueId() == null) {
        LOGGER.warn("Ignoring snapshot with null unique identifier {}", snapshot.getName());
        continue;
      }
      if (StringUtils.isBlank(snapshot.getName())) {
        LOGGER.warn("Ignoring snapshot {} with no name", snapshot.getUniqueId());
        continue;
      }
      if (GUID_PATTERN.matcher(snapshot.getName()).find()) {
        LOGGER.debug("Ignoring snapshot which appears to have an auto-generated name: {}", snapshot.getName());
        continue;
      }
      final String basisViewName = snapshot.getBasisViewName() != null ? snapshot.getBasisViewName() : "unknown";
      snapshotsByBasisView.put(basisViewName, snapshot);
    }
    // list of maps for each basis view: {"basisViewName": basisViewName, "snapshots", [...]}
    final List<Map<String, Object>> basisViewSnapshotList = new ArrayList<>();
    for (final String basisViewName : snapshotsByBasisView.keySet()) {
      final Collection<ManageableMarketDataSnapshot> viewSnapshots = snapshotsByBasisView.get(basisViewName);
      // list of maps containing snapshot IDs and names: {"id", snapshotId, "name", snapshotName}
      final List<Map<String, Object>> snapshotsList = new ArrayList<>(viewSnapshots.size());
      for (final ManageableMarketDataSnapshot viewSnapshot : viewSnapshots) {
        // map for a single snapshot: {"id", snapshotId, "name", snapshotName}
        final Map<String, Object> snapshotMap =
            ImmutableMap.<String, Object>of(ID, viewSnapshot.getUniqueId(), NAME, viewSnapshot.getName());
        snapshotsList.add(snapshotMap);
      }
      basisViewSnapshotList.add(ImmutableMap.of(BASIS_VIEW_NAME, basisViewName, SNAPSHOTS, snapshotsList));
    }
    return new JSONArray(basisViewSnapshotList).toString();
  }

  /**
   * Returns the version history of a market data snapshot.
   * @param snapshotId An snapshot {@link ObjectId}
   * @return JSON array of the snapshot's history
   * <pre>
   *   [{"uniqueId": "DbSnp~12345~2",
   *     "correctionFrom": "2012-05-23T10:54:10.124293Z",
   *     "correctionTo": null,
   *     "versionFrom": "2012-05-23T10:54:10.124293Z",
   *     "versionTo": null}]
   * </pre>
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{snapshotId}")
  public String getMarketDataSnapshotHistory(@PathParam("snapshotId") final String snapshotId) {
    final ObjectId id = ObjectId.parse(snapshotId);
    final MarketDataSnapshotHistoryResult result = _snapshotMaster.history(new MarketDataSnapshotHistoryRequest(id));
    final List<MarketDataSnapshotDocument> documents = result.getDocuments();
    final List<Map<String, Object>> json = Lists.newArrayListWithCapacity(documents.size());
    for (final MarketDataSnapshotDocument document : documents) {
      final Map<String, Object> map = Maps.newHashMapWithExpectedSize(5);
      map.put("uniqueId", document.getUniqueId());
      map.put("versionFrom", document.getVersionFromInstant());
      map.put("versionTo", document.getVersionToInstant());
      map.put("correctionFrom", document.getCorrectionFromInstant());
      map.put("correctionTo", document.getCorrectionToInstant());
      json.add(map);
    }
    return new JSONArray(json).toString();
  }
}
