/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Loads in a file containing either a single JodaXML or FudgeXML encoded config and updates the config master. Can be provided with a hint type if the JodaXML
 * messages don't contain a type attribute on the bean element.
 */
public class SingleConfigLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SingleConfigLoader.class);
  private final ConfigMaster _configMaster;
  private final ConventionMaster _conventionMaster;
  private final MarketDataSnapshotMaster _marketDataSnapshotMaster;
  private final boolean _doNotUpdateExisting;
  private final ConfigSource _configSource;
  private final SecurityMaster _securityMaster;

  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  private static final String DEFAULT_HTS_RATING_NAME = "DEFAULT_TSS_CONFIG";
  private static final String DEFAULT_CURRENCY_MATRIX_NAME = "BloombergLiveData";

  public SingleConfigLoader(final SecurityMaster securityMaster, final ConfigMaster configMaster, final ConfigSource configSource,
      final ConventionMaster conventionMaster, final MarketDataSnapshotMaster marketDataSnapshotMaster, final boolean doNotUpdateExisting) {
    _securityMaster = securityMaster;
    _configMaster = configMaster;
    _configSource = configSource;
    _conventionMaster = conventionMaster;
    _marketDataSnapshotMaster = marketDataSnapshotMaster;
    _doNotUpdateExisting = doNotUpdateExisting;
  }

  private ManageableConvention addOrUpdateConvention(final ManageableConvention convention) {
    final ConventionSearchRequest searchReq = new ConventionSearchRequest(convention.getExternalIdBundle());
    final ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    ConventionDocument match = null;
    for (final ConventionDocument doc : searchResult.getDocuments()) {
      if (doc.getConvention().getConventionType().equals(convention.getConventionType())) {
        if (match == null) {
          match = doc;
        } else {
          LOGGER.warn("Found more than one match for {} with type {}, changing first one", convention.getExternalIdBundle(), convention.getConventionType());
        }
      }
    }
    if (match != null) {
      if (_doNotUpdateExisting) {
        LOGGER.info("Found existing convention, skipping update");
        return match.getConvention();
      }
      LOGGER.info("Found existing convention, updating it");
      match.setConvention(convention);
      return _conventionMaster.update(match).getConvention();
    }
    LOGGER.info("No existing convention, creating a new one");
    final ConventionDocument doc = new ConventionDocument(convention);
    return _conventionMaster.add(doc).getConvention();
  }

  private ManageableSecurity addOrUpdateSecurity(final ManageableSecurity security) {
    final SecuritySearchRequest searchReq = new SecuritySearchRequest(security.getExternalIdBundle());
    final SecuritySearchResult search = _securityMaster.search(searchReq);
    if (search.getDocuments().size() > 0 && _doNotUpdateExisting) {
      LOGGER.info("Found existing convention, skipping update");
      return search.getFirstSecurity();
    }
    return SecurityMasterUtils.addOrUpdateSecurity(_securityMaster, security);
  }

  @SuppressWarnings("deprecation")
  private ManageableMarketDataSnapshot addOrUpdateSnapshot(final ManageableMarketDataSnapshot snapshot) {
    final MarketDataSnapshotSearchRequest searchReq = new MarketDataSnapshotSearchRequest();
    searchReq.setName(snapshot.getName());
    final MarketDataSnapshotSearchResult searchResult = _marketDataSnapshotMaster.search(searchReq);
    MarketDataSnapshotDocument match = null;
    for (final MarketDataSnapshotDocument doc : searchResult.getDocuments()) {
      if (doc.getSnapshot().getBasisViewName().equals(snapshot.getBasisViewName())) {
        if (match == null) {
          match = doc;
        } else {
          LOGGER.warn("Found more than one matching market data snapshot for {} with type {}, changing first one", snapshot.getName(),
              snapshot.getBasisViewName());
        }
      }
    }
    if (match != null) {
      if (_doNotUpdateExisting) {
        LOGGER.info("Found existing market data snapshot, skipping update");
        return match.getSnapshot();
      }
      LOGGER.info("Found existing market data snapshot, updating it");
      match.setSnapshot(snapshot);
      return _marketDataSnapshotMaster.update(match).getSnapshot();
    }
    LOGGER.info("No existing market data snapshot, creating a new one");
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
    return _marketDataSnapshotMaster.add(doc).getSnapshot();
  }

  public <T> void loadConfig(final InputStream is, final Class<T> hintType) {
    final T config = JodaBeanSerialization.deserializer().xmlReader().read(is, hintType);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof ManageableMarketDataSnapshot) {
      addOrUpdateSnapshot((ManageableMarketDataSnapshot) config);
    } else if (config instanceof ManageableSecurity) {
      addOrUpdateSecurity((ManageableSecurity) config);
    } else if (config instanceof CurrencyPairs) {
      final ConfigItem<?> item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof HistoricalTimeSeriesRating) {
      final ConfigItem<?> item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof CurrencyMatrix) {
      final ConfigItem<?> item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof Bean) {
      final ConfigItem<T> item = ConfigItem.of(config);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else {
      LOGGER.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }

  public void loadConfig(final InputStream is) {
    final Object config = JodaBeanSerialization.deserializer().xmlReader().read(is);
    if (config instanceof ManageableConvention) {
      addOrUpdateConvention((ManageableConvention) config);
    } else if (config instanceof ManageableMarketDataSnapshot) {
      addOrUpdateSnapshot((ManageableMarketDataSnapshot) config);
    } else if (config instanceof ManageableSecurity) {
      addOrUpdateSecurity((ManageableSecurity) config);
    } else if (config instanceof CurrencyPairs) {
      final ConfigItem<?> item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof HistoricalTimeSeriesRating) {
      final ConfigItem<?> item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof CurrencyMatrix) {
      final ConfigItem<?> item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME, CurrencyMatrix.class);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else if (config instanceof Bean) {
      final ConfigItem<?> item = ConfigItem.of(config);
      if (_doNotUpdateExisting && configExists(item)) {
        LOGGER.info("Existing config present, skipping");
      } else {
        ConfigMasterUtils.storeByName(_configMaster, item);
      }
    } else {
      LOGGER.error("Unsupported type {} is not a JodaBean", config.getClass());
    }
  }

  public void loadFudgeConfig(final InputStream is) {
    @SuppressWarnings("resource")
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(FUDGE_CONTEXT, new InputStreamReader(is)));
    final FudgeMsg message = fmr.nextMessage();

    final Object config = FUDGE_CONTEXT.fromFudgeMsg(message);
    ConfigItem<?> item;
    if (config instanceof CurrencyPairs) {
      item = ConfigItem.of(config, CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    } else if (config instanceof HistoricalTimeSeriesRating) {
      item = ConfigItem.of(config, DEFAULT_HTS_RATING_NAME);
    } else if (config instanceof CurrencyMatrix) {
      item = ConfigItem.of(config, DEFAULT_CURRENCY_MATRIX_NAME, CurrencyMatrix.class);
    } else {
      item = ConfigItem.of(config);
    }
    if (_doNotUpdateExisting && configExists(item)) {
      LOGGER.info("Existing config present, skipping");
    } else {
      ConfigMasterUtils.storeByName(_configMaster, item);
    }
  }

  private boolean configExists(final ConfigItem<?> configItem) {
    return _configSource.getLatestByName(configItem.getType(), configItem.getName()) != null;
  }
}
