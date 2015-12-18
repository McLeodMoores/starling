/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
// Copied from integration
package com.opengamma.integration.tool.config;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Class that saves a range of configuration objects into a file in XML format.
 */
public class ConfigSaver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSaver.class);
  private final ConfigMaster _configMaster;
  private final PortfolioMaster _portfolioMaster;
  private final List<String> _names;
  private final List<String> _types;
  private final boolean _portPortfolioRefs;
  private final boolean _verbose;
  private final ConfigSearchSortOrder _order;

  /**
   * Process items in name order.
   *
   * @param configMaster  the config master
   * @param portfolioMaster  the portfolio master
   * @param names  names to search for
   * @param types  types to search for
   * @param portPortfolioRefs  true if the portfolio references should be ported
   * @param verbose  true if the verbose output is required
   */
  public ConfigSaver(final ConfigMaster configMaster, final PortfolioMaster portfolioMaster, final List<String> names, final List<String> types,
      final boolean portPortfolioRefs, final boolean verbose) {
    this(configMaster, portfolioMaster, names, types, portPortfolioRefs, verbose, ConfigSearchSortOrder.NAME_ASC);
  }

  /**
   * Process items.
   *
   * @param configMaster  the config master
   * @param portfolioMaster  the portfolio master
   * @param names  names to search for
   * @param types  types to search for
   * @param portPortfolioRefs  true if the portfolio references should be ported
   * @param verbose  true if the verbose output is required
   * @param order  the config item search order.
   */
  public ConfigSaver(final ConfigMaster configMaster, final PortfolioMaster portfolioMaster, final List<String> names, final List<String> types,
      final boolean portPortfolioRefs, final boolean verbose, final ConfigSearchSortOrder order) {
    _configMaster = configMaster;
    _portfolioMaster = portfolioMaster;
    _names = names;
    _types = types;
    _portPortfolioRefs = portPortfolioRefs;
    _verbose = verbose;
    _order = order;
  }

  public void saveConfigs(final PrintStream outputStream) {
    final List<ConfigEntry> allConfigs = getAllConfigs();
    if (_verbose) {
      LOGGER.info("Matched " + allConfigs.size() + " configurations");
    }
    try (FudgeXMLStreamWriter xmlStreamWriter = new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(), new OutputStreamWriter(outputStream))) {
      final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
      final FlexiBean wrapper = new FlexiBean();
      wrapper.set("configs", allConfigs);
      if (_portPortfolioRefs) {
        final Map<UniqueId, String> idToPortfolioMap = getPortfolioNameMap(allConfigs);
        wrapper.set("idToPortfolioMap", idToPortfolioMap);
      }
      final MutableFudgeMsg msg = serializer.objectToFudgeMsg(wrapper);
      try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(xmlStreamWriter)) {
        fudgeMsgWriter.writeMessage(msg);
        fudgeMsgWriter.close();
      } catch (final Exception e) {
        LOGGER.error(e.getMessage());
      }
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private Map<UniqueId, String> getPortfolioNameMap(final List<ConfigEntry> configEntries) {
    final Map<UniqueId, String> idToPortfolioNameMap = Maps.newHashMap();
    for (final ConfigEntry configEntry : configEntries) {
      if (configEntry.getObject() instanceof ViewDefinition) {
        final ViewDefinition viewDefinition = (ViewDefinition) configEntry.getObject();
        final String portfolioName = getPortfolioName(viewDefinition.getPortfolioId());
        if (portfolioName != null) {
          idToPortfolioNameMap.put(viewDefinition.getPortfolioId(), portfolioName);
        } else {
          if (_verbose) {
            LOGGER.warn("Couldn't find portfolio for id in view definition called " + viewDefinition.getName());
          }
        }
      }
    }
    return idToPortfolioNameMap;
  }

  private String getPortfolioName(final UniqueId uniqueId) {
    if (uniqueId != null) {
      try {
        final PortfolioDocument portfolioDocument = _portfolioMaster.get(uniqueId);
        if (portfolioDocument != null) {
          return portfolioDocument.getPortfolio().getName();
        }
      } catch (final DataNotFoundException dnfe) {
        if (_verbose) {
          LOGGER.warn("Couldn't find portfolio for " + uniqueId);
        }
      }
    }
    return null;
  }

  private List<ConfigEntry> getAllConfigs() {
    final List<ConfigEntry> configsToSave = new ArrayList<ConfigEntry>();
    if (_types.size() > 0) {
      for (final String type : _types) {
        try {
          final Class<?> clazz = Class.forName(type);
          if (_names.size() > 0) {
            for (final String name : _names) {
              configsToSave.addAll(getConfigs(clazz, name));
            }
          } else {
            configsToSave.addAll(getConfigs(clazz));
          }
        } catch (final ClassNotFoundException cnfe) {
          LOGGER.error("Could not find class called " + type + " aborting");
          System.exit(1);
        }
      }
    } else {
      if (_names.size() > 0) {
        for (final String name : _names) {
          configsToSave.addAll(getConfigs(name));
        }
      } else {
        configsToSave.addAll(getConfigs());
      }
    }
    return configsToSave;
  }

  private List<ConfigEntry> getConfigs(final Class<?> type, final String name) {
    final ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(type);
    searchReq.setName(name);
    final ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }

  private List<ConfigEntry> getConfigs(final Class<?> type) {
    final ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(type);
    final ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }

  private List<ConfigEntry> getConfigs(final String name) {
    final ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setName(name);
    searchReq.setType(Object.class);
    final ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }

  private List<ConfigEntry> getConfigs() {
    final ConfigSearchRequest<Object> searchReq = createSearchRequest();
    searchReq.setType(Object.class);
    final ConfigSearchResult<Object> searchResult = _configMaster.search(searchReq);
    return docsToConfigEntries(searchResult);
  }


  /**
   * @return a search request with defaults set
   */
  private ConfigSearchRequest<Object> createSearchRequest() {
    final ConfigSearchRequest<Object> searchRequest = new ConfigSearchRequest<Object>();
    searchRequest.setSortOrder(_order);
    return searchRequest;
  }

  private List<ConfigEntry> docsToConfigEntries(final ConfigSearchResult<Object> searchResult) {
    final List<ConfigEntry> results = new ArrayList<ConfigEntry>();
    for (final ConfigItem<Object> doc : searchResult.getValues()) {
      final ConfigEntry configEntry = new ConfigEntry();
      configEntry.setName(doc.getName());
      configEntry.setType(doc.getType().getCanonicalName());
      configEntry.setObject(doc.getValue());
      results.add(configEntry);
    }
    return results;
  }

}
