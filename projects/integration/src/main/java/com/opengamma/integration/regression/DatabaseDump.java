/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static com.google.common.collect.Iterables.transform;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Dumps all the data required to run views from the database into Fudge XML files.
 */
/* package */class DatabaseDump {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDump.class);

  private final RegressionIO _io;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HolidayMaster _holidayMaster;
  private final ExchangeMaster _exchangeMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final ConventionMaster _conventionMaster;
  private final MasterQueryManager _masterQueryManager;
  private final LegalEntityMaster _legalEntityMaster;
  private final IdMappings _idMappings;
  private final Map<String, AtomicInteger> _nextIdByType = new HashMap<>();

  /* package */ DatabaseDump(final String outputDir,
      final SecurityMaster securityMaster,
      final PositionMaster positionMaster,
      final PortfolioMaster portfolioMaster,
      final ConfigMaster configMaster,
      final HistoricalTimeSeriesMaster timeSeriesMaster,
      final HolidayMaster holidayMaster,
      final ExchangeMaster exchangeMaster,
      final MarketDataSnapshotMaster snapshotMaster,
      final LegalEntityMaster legalEntityMaster,
      final ConventionMaster conventionMaster) {
    this(outputDir,
        securityMaster,
        positionMaster,
        portfolioMaster,
        configMaster,
        timeSeriesMaster,
        holidayMaster,
        exchangeMaster,
        snapshotMaster,
        legalEntityMaster,
        conventionMaster,
        MasterQueryManager.queryAll());
  }

  /* package */ DatabaseDump(final String outputDir,
      final SecurityMaster securityMaster,
      final PositionMaster positionMaster,
      final PortfolioMaster portfolioMaster,
      final ConfigMaster configMaster,
      final HistoricalTimeSeriesMaster timeSeriesMaster,
      final HolidayMaster holidayMaster,
      final ExchangeMaster exchangeMaster,
      final MarketDataSnapshotMaster snapshotMaster,
      final LegalEntityMaster legalEntityMaster,
      final ConventionMaster conventionMaster,
      final MasterQueryManager masterFilterManager) {
    this(new SubdirsRegressionIO(new File(outputDir), new FudgeXMLFormat(), true),
        securityMaster,
        positionMaster,
        portfolioMaster,
        configMaster,
        timeSeriesMaster,
        holidayMaster,
        exchangeMaster,
        snapshotMaster,
        legalEntityMaster,
        conventionMaster,
        masterFilterManager);
  }

  /* package */ DatabaseDump(final RegressionIO io,
      final SecurityMaster securityMaster,
      final PositionMaster positionMaster,
      final PortfolioMaster portfolioMaster,
      final ConfigMaster configMaster,
      final HistoricalTimeSeriesMaster timeSeriesMaster,
      final HolidayMaster holidayMaster,
      final ExchangeMaster exchangeMaster,
      final MarketDataSnapshotMaster snapshotMaster,
      final LegalEntityMaster legalEntityMaster,
      final ConventionMaster conventionMaster,
      final MasterQueryManager masterQueryManager) {
    _io = ArgumentChecker.notNull(io, "io");
    _legalEntityMaster = ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    _snapshotMaster = ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _exchangeMaster = ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    _holidayMaster = ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _timeSeriesMaster = ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    _positionMaster = ArgumentChecker.notNull(positionMaster, "positionMaster");
    _portfolioMaster = ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
    _masterQueryManager = ArgumentChecker.notNull(masterQueryManager, "masterQueryManager");
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");

    final ConfigItem<IdMappings> mappingsConfigItem = RegressionUtils.loadIdMappings(_configMaster);
    if (mappingsConfigItem != null) {
      _idMappings = mappingsConfigItem.getValue();
    } else {
      _idMappings = new IdMappings();
    }
    LOGGER.info("Dumping database to {}", _io.getBaseFile().getAbsolutePath());
  }

  public static void main(final String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("arguments: dataDirectory serverUrl");
      System.exit(1);
    }
    final String dataDir = args[0];
    final SubdirsRegressionIO io = new SubdirsRegressionIO(new File(dataDir), new FudgeXMLFormat(), true);
    final String serverUrl = args[1];
    int exitCode = 0;
    try (RemoteServer server = RemoteServer.create(serverUrl)) {
      final DatabaseDump databaseDump = new DatabaseDump(io, server.getSecurityMaster(), server.getPositionMaster(), server.getPortfolioMaster(),
          server.getConfigMaster(),
          server.getHistoricalTimeSeriesMaster(), server.getHolidayMaster(), server.getExchangeMaster(), server.getMarketDataSnapshotMaster(),
          server.getLegalEntityMaster(),
          server.getConventionMaster(), MasterQueryManager.queryAll());
      io.beginWrite();
      try {
        databaseDump.dumpDatabase();
      } finally {
        io.endWrite();
      }
    } catch (final Exception e) {
      LOGGER.warn("Failed to write data", e);
      exitCode = 1;
    }
    System.exit(exitCode);
  }

  /**
   * Dump db to injected {@link RegressionIO} instance. Note the regression io instance should have already been opened before this method is called.
   * 
   * @throws IOException
   *           if an IO exception is thrown
   */
  public void dumpDatabase() throws IOException {
    final Map<ObjectId, Integer> ids = Maps.newHashMap(_idMappings.getIds());
    ids.putAll(writeSecurities());
    ids.putAll(writePositions());
    ids.putAll(writePortfolios());
    ids.putAll(writeConfig());
    ids.putAll(writeTimeSeries());
    ids.putAll(writeHolidays());
    ids.putAll(writeExchanges());
    ids.putAll(writeSnapshots());
    ids.putAll(writeLegalEntities());
    ids.putAll(writeConventions());
    int maxId = _idMappings.getMaxId();
    for (final Integer id : ids.values()) {
      if (id > maxId) {
        maxId = id;
      }
    }
    final IdMappings idMappings = new IdMappings(ids, maxId);
    _io.write(null, idMappings, RegressionUtils.ID_MAPPINGS_IDENTIFIER);
  }

  private Map<ObjectId, Integer> writeSecurities() throws IOException {
    final Iterable<SecurityDocument> result = _masterQueryManager.getSecurityQuery().apply(_securityMaster);
    return write(transform(result, new SecurityTransformer()), "securities", "sec");
  }

  private Map<ObjectId, Integer> writePositions() throws IOException {
    final Iterable<PositionDocument> result = _masterQueryManager.getPositionQuery().apply(_positionMaster);
    return write(transform(result, new PositionTransformer()), "positions", "pos");
  }

  private Map<ObjectId, Integer> writeConfig() throws IOException {
    final Iterable<ConfigDocument> result = _masterQueryManager.getConfigQuery().apply(_configMaster);
    return write(transform(result, new ConfigTransformer()), "configs", "cfg");
  }

  private Map<ObjectId, Integer> writePortfolios() throws IOException {
    final Iterable<PortfolioDocument> result = _masterQueryManager.getPortfolioQuery().apply(_portfolioMaster);
    return write(transform(result, new PortfolioTransformer()), "portfolios", "prt");
  }

  private Map<ObjectId, Integer> writeTimeSeries() throws IOException {
    final Iterable<HistoricalTimeSeriesInfoDocument> result = _masterQueryManager.getHtsQuery().apply(_timeSeriesMaster);
    return write(transform(result, new TimeSeriesTransformer()), "timeseries", "hts");
  }

  private Map<ObjectId, Integer> writeHolidays() throws IOException {
    final Iterable<HolidayDocument> result = _masterQueryManager.getHolidayQuery().apply(_holidayMaster);
    return write(transform(result, new HolidayTransformer()), "holidays", "hol");
  }

  private Map<ObjectId, Integer> writeExchanges() throws IOException {
    final Iterable<ExchangeDocument> result = _masterQueryManager.getExchangeQuery().apply(_exchangeMaster);
    return write(transform(result, new ExchangeTransformer()), "exchanges", "exg");
  }

  private Map<ObjectId, Integer> writeSnapshots() throws IOException {
    final Iterable<MarketDataSnapshotDocument> result = _masterQueryManager.getMarketDataSnapshotQuery().apply(_snapshotMaster);
    return write(transform(result, new SnapshotTransformer()), "snapshots", "snp");
  }

  private Map<ObjectId, Integer> writeLegalEntities() throws IOException {
    final Iterable<LegalEntityDocument> result = _masterQueryManager.getLegalEntityQuery().apply(_legalEntityMaster);
    return write(transform(result, new LegalEntityTransformer()), "legalentities", "len");
  }

  private Map<ObjectId, Integer> writeConventions() throws IOException {
    final Iterable<ConventionDocument> result = _masterQueryManager.getConventionQuery().apply(_conventionMaster);
    return write(transform(result, new ConventionTransformer()), "conventions", "con");
  }

  private Map<ObjectId, Integer> write(final Iterable<? extends UniqueIdentifiable> objects, final String type, final String prefix) throws IOException {
    final List<UniqueIdentifiable> sortedObjects = Lists.newArrayList(objects);
    // sort the objects so two dumps of the same database put the same objects in the same files
    Collections.sort(sortedObjects, new UniqueIdentifiableComparator());
    LOGGER.info("Writing {} to {}", type, _io.getBaseFile().getAbsolutePath());
    final Map<ObjectId, Integer> ids = Maps.newHashMap();
    final Map<String, Object> toWrite = Maps.newHashMap();
    int count = 0;
    for (final UniqueIdentifiable object : sortedObjects) {
      final ObjectId objectId = object.getUniqueId().getObjectId();
      final Integer previousId = _idMappings.getId(objectId);
      int id;
      if (previousId == null) {
        id = getNextId(type);
        ids.put(objectId, id);
      } else {
        id = previousId;
      }
      toWrite.put(prefix + id, object);
      count++;
    }
    _io.write(type, toWrite);
    LOGGER.info("Wrote {} objects", count);
    return ids;
  }

  private class SecurityTransformer implements Function<SecurityDocument, ManageableSecurity> {
    @Override
    public ManageableSecurity apply(final SecurityDocument input) {
      return input.getSecurity();
    }
  }

  private class PositionTransformer implements Function<PositionDocument, ManageablePosition> {
    @Override
    public ManageablePosition apply(final PositionDocument input) {
      return input.getPosition();
    }
  }

  private class PortfolioTransformer implements Function<PortfolioDocument, ManageablePortfolio> {
    @Override
    public ManageablePortfolio apply(final PortfolioDocument input) {
      return input.getPortfolio();
    }
  }

  private class ConfigTransformer implements Function<ConfigDocument, ConfigItem<?>> {

    @Override
    public ConfigItem<?> apply(final ConfigDocument input) {
      return input.getConfig();
    }
  }

  private class TimeSeriesTransformer implements Function<HistoricalTimeSeriesInfoDocument, TimeSeriesWithInfo> {

    @Override
    public TimeSeriesWithInfo apply(final HistoricalTimeSeriesInfoDocument infoDoc) {
      final ManageableHistoricalTimeSeriesInfo info = infoDoc.getInfo();
      final ManageableHistoricalTimeSeries timeSeries = _timeSeriesMaster.getTimeSeries(info.getTimeSeriesObjectId(), VersionCorrection.LATEST);
      return new TimeSeriesWithInfo(info, timeSeries);
    }
  }

  private class HolidayTransformer implements Function<HolidayDocument, ManageableHoliday> {
    @Override
    public ManageableHoliday apply(final HolidayDocument input) {
      return input.getHoliday();
    }
  }

  private class ExchangeTransformer implements Function<ExchangeDocument, ManageableExchange> {
    @Override
    public ManageableExchange apply(final ExchangeDocument input) {
      return input.getExchange();
    }
  }

  private class SnapshotTransformer implements Function<MarketDataSnapshotDocument, ManageableMarketDataSnapshot> {
    @Override
    public ManageableMarketDataSnapshot apply(final MarketDataSnapshotDocument input) {
      return input.getSnapshot();
    }
  }

  private class LegalEntityTransformer implements Function<LegalEntityDocument, ManageableLegalEntity> {
    @Override
    public ManageableLegalEntity apply(final LegalEntityDocument input) {
      return input.getLegalEntity();
    }
  }

  private class ConventionTransformer implements Function<ConventionDocument, ManageableConvention> {
    @Override
    public ManageableConvention apply(final ConventionDocument input) {
      return input.getConvention();
    }
  }

  private class UniqueIdentifiableComparator implements Comparator<UniqueIdentifiable> {

    @Override
    public int compare(final UniqueIdentifiable o1, final UniqueIdentifiable o2) {
      return o1.getUniqueId().compareTo(o2.getUniqueId());
    }
  }

  private int getNextId(final String type) {
    AtomicInteger nextId = _nextIdByType.get(type);

    if (nextId == null) {
      nextId = new AtomicInteger();
      _nextIdByType.put(type, nextId);
    }
    return nextId.getAndIncrement();
  }
}
