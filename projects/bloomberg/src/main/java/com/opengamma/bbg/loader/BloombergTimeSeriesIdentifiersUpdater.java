/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Updates the timeseries identifiers with loaded identifiers from Bloomberg.
 */
public class BloombergTimeSeriesIdentifiersUpdater {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergTimeSeriesIdentifiersUpdater.class);

  /**
   * The Spring config file.
   */
  static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-timeseries-identifier-context.xml";

  /**
   * The master.
   */
  private final HistoricalTimeSeriesMaster _htsMaster;
  /**
   * The provider of identifiers.
   */
  private final ExternalIdResolver _bbgIdentifierProvider;

  /**
   * Creates a new instance of the updater.
   *
   * @param htsMaster
   *          the historical time-series master, not null
   * @param bbgIdentifierProvider
   *          the identifier provider, not null
   */
  public BloombergTimeSeriesIdentifiersUpdater(final HistoricalTimeSeriesMaster htsMaster, final ExternalIdResolver bbgIdentifierProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(bbgIdentifierProvider, "identifierProvider");
    _htsMaster = htsMaster;
    _bbgIdentifierProvider = bbgIdentifierProvider;
  }

  // -------------------------------------------------------------------------
  /**
   * Main processing.
   */
  public void run() {
    // fetch the documents to update
    final Iterable<HistoricalTimeSeriesInfoDocument> documents = getCurrentTimeSeriesDocuments();

    // find the BUIDs
    final Map<ExternalId, HistoricalTimeSeriesInfoDocument> buidDocMap = extractBuids(documents);
    final Set<ExternalId> buids = new HashSet<>(buidDocMap.keySet());

    // query Bloomberg
    final Map<ExternalId, ExternalIdBundleWithDates> buidToUpdated = _bbgIdentifierProvider.getExternalIds(buids);
    for (final Entry<ExternalId, ExternalIdBundleWithDates> entry : buidToUpdated.entrySet()) {
      entry.setValue(BloombergDataUtils.addTwoDigitYearCode(entry.getValue()));
    }

    // update the database
    updateIdentifiers(buidDocMap, buidToUpdated);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets all the current Bloomberg-based time-series.
   *
   * @return the current documents, not null
   */
  private Iterable<HistoricalTimeSeriesInfoDocument> getCurrentTimeSeriesDocuments() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    return HistoricalTimeSeriesInfoSearchIterator.iterable(_htsMaster, request);
  }

  // -------------------------------------------------------------------------
  /**
   * Extracts the BUID from each document.
   *
   * @param documents
   *          the documents, not null
   * @return the map of BIUD to unique identifier, not null
   */
  private Map<ExternalId, HistoricalTimeSeriesInfoDocument> extractBuids(final Iterable<HistoricalTimeSeriesInfoDocument> documents) {
    final Map<ExternalId, HistoricalTimeSeriesInfoDocument> buids = Maps.newHashMap();
    for (final HistoricalTimeSeriesInfoDocument doc : documents) {
      final ExternalIdBundleWithDates identifierBundleWithDates = doc.getInfo().getExternalIdBundle();
      final ExternalIdBundle bundle = identifierBundleWithDates.toBundle();
      final ExternalId buid = bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID);
      if (buid == null) {
        throw new OpenGammaRuntimeException("no buid for " + bundle);
      }
      buids.put(buid, doc);
    }
    return buids;
  }

  // -------------------------------------------------------------------------
  /**
   * Updates the identifiers.
   *
   * @param buidDocMap
   *          the map from BUID to document, not null
   * @param buidToUpdated
   *          the map from BUID to updated identifier, not null
   */
  private void updateIdentifiers(
      final Map<ExternalId, HistoricalTimeSeriesInfoDocument> buidDocMap,
      final Map<ExternalId, ExternalIdBundleWithDates> buidToUpdated) {
    for (final Entry<ExternalId, ExternalIdBundleWithDates> entry : buidToUpdated.entrySet()) {
      final HistoricalTimeSeriesInfoDocument doc = buidDocMap.get(entry.getKey());
      final ExternalIdBundleWithDates updatedId = entry.getValue();
      if (doc != null && doc.getInfo().getExternalIdBundle().equals(updatedId) == false) {
        doc.getInfo().setExternalIdBundle(updatedId);
        LOGGER.debug("Updated {} with {}", doc.getUniqueId(), updatedId);
        _htsMaster.update(doc);
      }
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Main method to run the updater. This uses the updater configured by Spring.
   *
   * @param args
   *          not used
   */
  public static void main(final String[] args) { // CSIGNORE
    PlatformConfigUtils.configureSystemProperties();
    final BloombergTimeSeriesIdentifiersUpdater updater = loadUpdater();
    updater.run();
  }

  /**
   * Gets the loader from Spring config.
   *
   * @return the identifier loader, not null
   */
  private static BloombergTimeSeriesIdentifiersUpdater loadUpdater() {
    final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    final BloombergTimeSeriesIdentifiersUpdater loader = (BloombergTimeSeriesIdentifiersUpdater) context.getBean("identifiersLoader");
    return loader;
  }

}
