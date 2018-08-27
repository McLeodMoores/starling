/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Class to load configurations from an input stream
 */
public class ConfigLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
  private final ConfigMaster _configMaster;
  private final PortfolioMaster _portfolioMaster;
  private final boolean _actuallyStore;
  private final boolean _verbose;
  private final boolean _attemptToPortPortfolioIds;

  public ConfigLoader(final ConfigMaster configMaster, final PortfolioMaster portfolioMaster, final boolean attemptToPortPortfolioIds,
                      final boolean actuallyStore, final boolean verbose) {
    _configMaster = configMaster;
    _portfolioMaster = portfolioMaster;
    _attemptToPortPortfolioIds = attemptToPortPortfolioIds;
    _actuallyStore = actuallyStore;
    _verbose = verbose;
  }

  public void loadConfig(final InputStream inputStream) {
    final FudgeXMLStreamReader xmlStreamReader = new FudgeXMLStreamReader(OpenGammaFudgeContext.getInstance(), new InputStreamReader(new BufferedInputStream(inputStream)));
    final FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlStreamReader);
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final FudgeMsg configsMessage = fudgeMsgReader.nextMessage();
    if (configsMessage == null) {
      LOGGER.error("Error reading first message from XML stream");
      return;
    }
    final Object object = deserializer.fudgeMsgToObject(FlexiBean.class, configsMessage);
    if (!(object instanceof FlexiBean)) {
      LOGGER.error("XML Stream deserialised to object of type " + object.getClass() + ": " + object.toString());
      return;
    }
    final FlexiBean wrapper = (FlexiBean) object;
    if (!wrapper.contains("configs")) {
      LOGGER.error("File stream does not contain configs element");
      return;
    }
    @SuppressWarnings("unchecked")
    final
    List<ConfigEntry> configs = (List<ConfigEntry>) wrapper.get("configs");
    if (wrapper.contains("idToPortfolioMap")) {
      @SuppressWarnings("unchecked")
      final
      Map<UniqueId, String> idToPortfolioMap = (Map<UniqueId, String>) wrapper.get("idToPortfolioMap");
      if (idToPortfolioMap == null) {
        LOGGER.warn("Apparently corrupt portfolio id -> name map, won't attempt to port portfolio ids");
        loadConfigs(configs, Collections.<UniqueId, String>emptyMap());
      } else {
        loadConfigs(configs, idToPortfolioMap);
      }
    } else {
      loadConfigs(configs, Collections.<UniqueId, String>emptyMap());
    }

  }

  private void loadConfigs(final List<ConfigEntry> configs, final Map<UniqueId, String> idNameMap) {
    for (final ConfigEntry entry : configs) {
      try {
        final Class<?> clazz = Class.forName(entry.getType());
        Object object = entry.getObject();
        if (object instanceof ViewDefinition) {
          if (_attemptToPortPortfolioIds) {
            object = attemptToPortPortfolioIds((ViewDefinition) object, idNameMap);
          }
        }
        final ConfigItem<Object> item = ConfigItem.of(object, entry.getName(), clazz);
        if (_actuallyStore) {
          ConfigMasterUtils.storeByName(_configMaster, item);
          if (_verbose) {
            LOGGER.info("Stored " + entry.getName() + " of type " + entry.getType());
          }
        } else {
          if (_verbose) {
            LOGGER.info("Simulated store " + entry.getName() + " of type " + entry.getType());
          }
        }

      } catch (final ClassNotFoundException ex) {
        LOGGER.error("Could not find class called " + entry.getType() + " skipping config " + entry.getName());
      }
    }
  }

  private ViewDefinition attemptToPortPortfolioIds(final ViewDefinition viewDefinition, final Map<UniqueId, String> idNameMap) {
    if (idNameMap.containsKey(viewDefinition.getPortfolioId())) {
      if (_verbose) {
        LOGGER.info("Attempting to port portfolio id " + viewDefinition.getPortfolioId());
      }
      UniqueId replacementId = lookupPortfolioByName(idNameMap.get(viewDefinition.getPortfolioId()));
      if (replacementId != null) {
        if (viewDefinition.getPortfolioId().isLatest()) {
          replacementId = replacementId.toLatest();
        }
        return viewDefinition.copyWith(viewDefinition.getName(), replacementId, viewDefinition.getMarketDataUser());
      }
    }
    return viewDefinition;
  }

  private UniqueId lookupPortfolioByName(final String name) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(name);
    final PortfolioSearchResult searchResult = _portfolioMaster.search(searchRequest);
    try {
      final ManageablePortfolio singlePortfolio = searchResult.getSinglePortfolio();
      if (_verbose) {
        LOGGER.info("Found portfolio called " + name + " mapping in it's id: " + singlePortfolio.getUniqueId());
      }
      return singlePortfolio.getUniqueId();
    } catch (final IllegalStateException ise) {
      LOGGER.warn("Found multiple portfolios called " + name + " so skipping");
      return null;
    } catch (final OpenGammaRuntimeException ogre) {
      if (searchResult.getDocuments().size() > 1) {
        LOGGER.warn("Found multiple portfolios called " + name + " so skipping");
      } else {
        LOGGER.warn("Didn't find a portfolio called " + name + " so skipping");
      }
      return null;
    }
  }
}
