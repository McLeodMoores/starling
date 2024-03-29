/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.redis.RedisConnector;

/**
 * Job that snapshot lastest market values in RedisServer and updates the timeseries master.
 */
public class RedisHtsSnapshotJob implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisHtsSnapshotJob.class);

  private HistoricalTimeSeriesMaster _htsMaster;
  private String _dataSource;
  private BlackList _dataFieldBlackList;
  private BlackList _schemeBlackList;
  private String _observationTime;
  private String _normalizationRuleSetId;
  private String _globalPrefix = "";
  private RedisConnector _redisConnector;
  private String _baseDir;

  /**
   * Gets the globalPrefix.
   * 
   * @return the globalPrefix
   */
  public String getGlobalPrefix() {
    return _globalPrefix;
  }

  /**
   * Sets the globalPrefix.
   * 
   * @param globalPrefix
   *          the globalPrefix
   */
  public void setGlobalPrefix(final String globalPrefix) {
    _globalPrefix = globalPrefix;
  }

  /**
   * Gets the htsMaster.
   * 
   * @return the htsMaster
   */
  public HistoricalTimeSeriesMaster getHtsMaster() {
    return _htsMaster;
  }

  /**
   * Gets the dataSource.
   * 
   * @return the dataSource
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the dataFieldBlackList.
   * 
   * @return the dataFieldBlackList
   */
  public BlackList getDataFieldBlackList() {
    return _dataFieldBlackList;
  }

  /**
   * Sets the dataFieldBlackList.
   * 
   * @param dataFieldBlackList
   *          the dataFieldBlackList
   */
  public void setDataFieldBlackList(final BlackList dataFieldBlackList) {
    _dataFieldBlackList = dataFieldBlackList;
  }

  /**
   * Gets the schemeBlackList.
   * 
   * @return the schemeBlackList
   */
  public BlackList getSchemeBlackList() {
    return _schemeBlackList;
  }

  /**
   * Sets the schemeBlackList.
   * 
   * @param schemeBlackList
   *          the schemeBlackList
   */
  public void setSchemeBlackList(final BlackList schemeBlackList) {
    _schemeBlackList = schemeBlackList;
  }

  /**
   * Gets the redisConnector.
   * 
   * @return the redisConnector
   */
  public RedisConnector getRedisConnector() {
    return _redisConnector;
  }

  /**
   * Sets the redisConnector.
   * 
   * @param redisConnector
   *          the redisConnector
   */
  public void setRedisConnector(final RedisConnector redisConnector) {
    _redisConnector = redisConnector;
  }

  /**
   * Sets the dataSource.
   * 
   * @param dataSource
   *          the dataSource
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
  }

  /**
   * Sets the observationTime.
   * 
   * @param observationTime
   *          the observationTime
   */
  public void setObservationTime(final String observationTime) {
    _observationTime = observationTime;
  }

  /**
   * Sets the normalizationRuleSetId.
   * 
   * @param normalizationRuleSetId
   *          the normalizationRuleSetId
   */
  public void setNormalizationRuleSetId(final String normalizationRuleSetId) {
    _normalizationRuleSetId = normalizationRuleSetId;
  }

  /**
   * Gets the observationTime.
   * 
   * @return the observationTime
   */
  public String getObservationTime() {
    return _observationTime;
  }

  /**
   * Gets the normalizationRuleSetId.
   * 
   * @return the normalizationRuleSetId
   */
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  /**
   * Gets the baseDir.
   * 
   * @return the baseDir
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the baseDir.
   * 
   * @param baseDir
   *          the baseDir
   */
  public void setBaseDir(final String baseDir) {
    _baseDir = baseDir;
  }

  @Override
  public void run() {
    validateState();

    // write a copy of redis lkv to disk
    final RedisLKVFileWriter snapshotFileWriter = new RedisLKVFileWriter();
    snapshotFileWriter.setBaseDir(new File(getBaseDir()));
    snapshotFileWriter.setDataFieldBlackList(EmptyBlackList.INSTANCE);
    snapshotFileWriter.setGlobalPrefix(getGlobalPrefix());
    snapshotFileWriter.setNormalizationRuleSetId(getNormalizationRuleSetId());
    snapshotFileWriter.setObservationTime(getObservationTime());
    snapshotFileWriter.setRedisConnector(getRedisConnector());
    snapshotFileWriter.setSchemeBlackList(EmptyBlackList.INSTANCE);
    snapshotFileWriter.run();

    final RedisLKVFileReader redisLKVFileReader = new RedisLKVFileReader(snapshotFileWriter.getOutputFile(), getSchemeBlackList(), getDataFieldBlackList());

    final Map<ExternalId, Map<String, Double>> redisLKV = redisLKVFileReader.getLastKnownValues();

    final AtomicLong tsCounter = new AtomicLong();
    final long startTime = System.nanoTime();
    for (final Entry<ExternalId, Map<String, Double>> lkvEntry : redisLKV.entrySet()) {
      updateTimeSeries(lkvEntry.getKey(), lkvEntry.getValue(), tsCounter);
    }
    final long stopTime = System.nanoTime();
    LOGGER.info("{}ms-Writing/Updating {} timeseries", (stopTime - startTime) / 1000000, tsCounter.get());
  }

  private void validateState() {
    ArgumentChecker.notNull(getNormalizationRuleSetId(), "normalization rule set Id");
    ArgumentChecker.notNull(getDataSource(), "dataSource");
    ArgumentChecker.notNull(getObservationTime(), "observation time");
    ArgumentChecker.notNull(getHtsMaster(), "historical timeseries master");
    ArgumentChecker.notNull(getRedisConnector(), "redis connector");
  }

  private void updateTimeSeries(final ExternalId externalId, final Map<String, Double> lkv, final AtomicLong tsCounter) {
    final HistoricalTimeSeriesMasterUtils htsMaster = new HistoricalTimeSeriesMasterUtils(getHtsMaster());
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    for (final Entry<String, Double> lkvEntry : lkv.entrySet()) {
      final String fieldName = lkvEntry.getKey();
      final Double value = lkvEntry.getValue();

      if (haveDataFieldBlackList() && _dataFieldBlackList.getBlackList().contains(fieldName.toUpperCase())) {
        continue;
      }
      if (value != null) {
        final String dataField = makeDataField(fieldName);
        String dataProvider = externalId.getScheme().getName();
        if ("SURF".equals(dataProvider.toUpperCase())) {
          dataProvider = "TULLETTPREBON";
        }
        LOGGER.debug("updating ts {}:{}/{}/{}/{} with {}:{}",
            new Object[] { externalId, getDataSource(), dataProvider, dataField, getObservationTime(), today, value });
        htsMaster.writeTimeSeriesPoint(makeDescription(externalId, dataField), getDataSource(), dataProvider,
            dataField, getObservationTime(), ExternalIdBundle.of(externalId), today, value);
        tsCounter.getAndAdd(1);
      }
    }
  }

  private boolean haveDataFieldBlackList() {
    return _dataFieldBlackList != null && _dataFieldBlackList.getBlackList() != null;
  }

  private String makeDataField(final String fieldName) {
    return fieldName.replaceAll("\\s+", "_").toUpperCase();
  }

  private String makeDescription(final ExternalId externalId, final String dataField) {
    return getDataSource() + "_" + externalId.getScheme() + "_" + externalId.getValue() + "_" + dataField;
  }

  public void setHistoricalTimeSeriesMaster(final HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }
}
