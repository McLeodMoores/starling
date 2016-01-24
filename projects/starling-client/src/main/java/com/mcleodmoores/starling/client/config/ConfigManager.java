/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.config;

import com.mcleodmoores.starling.client.portfolio.PortfolioKey;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for managing configurations.
 */
public class ConfigManager {
  /** A config master */
  private final ConfigMaster _configMaster;
  /** A config source */
  private final ConfigSource _configSource;

  /**
   * Public constructor when you have a ToolContext.
   * @param toolContext  the tool context, not null
   */
  public ConfigManager(final ToolContext toolContext) {
    ArgumentChecker.notNull(toolContext, "toolContext");
    _configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "toolContext.configMaster");
    _configSource = ArgumentChecker.notNull(toolContext.getConfigSource(), "toolContext.configSource");
  }

  /**
   * Public constructor with explicit dependencies provided.
   * @param configMaster  the configuration master, not null
   * @param configSource  the configuration source, not null
   */
  public ConfigManager(final ConfigMaster configMaster, final ConfigSource configSource) {
    _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
  }

  /**
   * Create a new view, using another view as a template and substitute the provided portfolio key in the resulting view.
   * The template view will not be modified.
   * @param templateViewKey  the key for the view to use as a template, not null
   * @param portfolioKey  the key for the portfolio that the new view should refer to, not null
   * @param viewPrefix  a prefix to add to the name of the template view when creating the new view name (typically a session token), not null or empty
   * @return the key for the new view
   */
  public ViewKey createTemplateView(final ViewKey templateViewKey, final PortfolioKey portfolioKey, final String viewPrefix) {
    ArgumentChecker.notNull(templateViewKey, "templateViewKey");
    ArgumentChecker.notNull(portfolioKey, "portfolioKey");
    ArgumentChecker.notEmpty(viewPrefix, "viewPrefix");
    if (!portfolioKey.hasUniqueId()) {
      throw new OpenGammaRuntimeException("Portfolio key must have been retrieved from database or persisted portfolio");
    }
    try {
      ViewDefinition viewDefinition;
      if (templateViewKey.hasUniqueId()) {
        viewDefinition = _configSource.getConfig(ViewDefinition.class, templateViewKey.getUniqueId());
      } else {
        viewDefinition = _configSource.getSingle(ViewDefinition.class, templateViewKey.getName(), VersionCorrection.LATEST);
      }
      viewDefinition.setUniqueId(null); // don't want to overwrite existing one!
      final String newViewName = viewPrefix + viewDefinition.getName();
      final ViewDefinition newViewDefinition = viewDefinition.copyWith(newViewName, portfolioKey.getUniqueId(), UserPrincipal.getLocalUser());
      final ConfigItem<ViewDefinition> newConfigItem = ConfigMasterUtils.storeByName(_configMaster, ConfigItem.of(newViewDefinition));
      return ViewKey.of(newViewName, newConfigItem.getUniqueId());
    } catch (final DataNotFoundException dnfe) {
      throw new OpenGammaRuntimeException("Could not find view definition " + templateViewKey, dnfe);
    }
  }

  /**
   * Delete an existing view.
   * @param viewKey  the key for the view, not null
   */
  public void deleteView(final ViewKey viewKey) {
    ArgumentChecker.notNull(viewKey, "viewKey");
    try {
      if (viewKey.hasUniqueId()) {
        _configMaster.remove(viewKey.getUniqueId());
      } else {
        final ViewDefinition single = _configSource.getSingle(ViewDefinition.class, viewKey.getName(), VersionCorrection.LATEST);
        _configMaster.remove(single.getUniqueId());
      }
    } catch (final DataNotFoundException dnfe) {
      throw new OpenGammaRuntimeException("Could not find view definition " + viewKey, dnfe);
    }
  }

}
