/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.PortfolioKey;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ConfigManager}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigManagerTest {
  /** A view definition */
  private static final ViewDefinition VIEW_DEFINITION;
  static {
    VIEW_DEFINITION = new ViewDefinition("Test", null, UserPrincipal.getLocalUser());
    VIEW_DEFINITION.setDefaultCurrency(Currency.USD);
    VIEW_DEFINITION.setMaxDeltaCalculationPeriod(500L);
    VIEW_DEFINITION.setMaxFullCalculationPeriod(500L);
    VIEW_DEFINITION.setMinDeltaCalculationPeriod(500L);
    VIEW_DEFINITION.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(VIEW_DEFINITION, "Test");
    VIEW_DEFINITION.addViewCalculationConfiguration(defaultCalculationConfig);
  }
  /** A view key */
  private static final ViewKey VIEW_KEY = ViewKey.of(VIEW_DEFINITION);
  /** The portfolio unique id */
  private static final UniqueId PORTFOLIO_UID = UniqueId.of("Test", "A");
  /** A portfolio key */
  private static final PortfolioKey PORTFOLIO_KEY = PortfolioKey.of("Portfolio", PORTFOLIO_UID);
  /** A view prefix */
  private static final String VIEW_PREFIX = "Prefix";

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToolContext() {
    new ConfigManager(null);
  }

  /**
   * Tests the behaviour when the tool context does not contain a config master.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigMaster1() {
    final ConfigSource configSource = new MasterConfigSource(new InMemoryConfigMaster());
    try (ToolContext toolContext = new ToolContext()) {
      toolContext.setConfigSource(configSource);
      new ConfigManager(toolContext);
    } catch (final Exception e) {
      throw e;
    }
  }

  /**
   * Tests the behaviour when the tool context does not contain a config source.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource1() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    try (ToolContext toolContext = new ToolContext()) {
      toolContext.setConfigMaster(configMaster);
      new ConfigManager(toolContext);
    } catch (final Exception e) {
      throw e;
    }
  }

  /**
   * Tests the behaviour when the config master is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigMaster2() {
    final ConfigSource configSource = new MasterConfigSource(new InMemoryConfigMaster());
    new ConfigManager(null, configSource);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource2() {
    new ConfigManager(new InMemoryConfigMaster(), null);
  }

  /**
   * Tests the behaviour when the template view key is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewKey1() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    configManager.createTemplateView(null, PORTFOLIO_KEY, VIEW_PREFIX);
  }

  /**
   * Tests the behaviour when the portfolio key is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPortfolioKey() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    configManager.createTemplateView(VIEW_KEY, null, VIEW_PREFIX);
  }

  /**
   * Tests the behaviour when the view prefix is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewPrefix() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    configManager.createTemplateView(VIEW_KEY, PORTFOLIO_KEY, null);
  }

  /**
   * Tests the behaviour when the view prefix is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyViewPrefix() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    configManager.createTemplateView(VIEW_KEY, PORTFOLIO_KEY, "");
  }

  /**
   * Tests the behaviour when the portfolio key does not have a unique id.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testPortfolioKeyNoUid() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    final PortfolioKey portfolioKey = PortfolioKey.of("Portfolio");
    configManager.createTemplateView(VIEW_KEY, portfolioKey, VIEW_PREFIX);
  }

  /**
   * Tests the behaviour when the view key is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewKey2() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    configManager.deleteView(null);
  }

  /**
   * Tests that a new view definition is created from a template view and that the original is unchanged.
   */
  @Test
  public void testViewCreation() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    // so that uid doesn't change on original
    final ViewDefinition originalView = VIEW_DEFINITION.copyWith(VIEW_DEFINITION.getName(), VIEW_DEFINITION.getPortfolioId(), VIEW_DEFINITION.getMarketDataUser());
    final ViewDefinition originalStoredView = ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(originalView)).getValue();
    // view key with uid set
    ViewKey originalStoredViewKey = ViewKey.of(originalStoredView);
    ViewKey newStoredViewKey = configManager.createTemplateView(originalStoredViewKey, PORTFOLIO_KEY, VIEW_PREFIX);
    assertEquals(newStoredViewKey.getName(), VIEW_PREFIX + originalStoredView.getName());
    assertTrue(newStoredViewKey.hasUniqueId());
    assertEquals(configSource.getLatestByName(ViewDefinition.class, originalStoredView.getName()), originalStoredView);
    ViewDefinition newStoredView = configSource.getConfig(ViewDefinition.class, newStoredViewKey.getUniqueId());
    assertEquals(newStoredView.getName(), VIEW_PREFIX + originalStoredView.getName());
    // reset name and uid and check everything else is the same
    ViewDefinition newStoredViewWithOriginalName = newStoredView.copyWith(VIEW_DEFINITION.getName(), VIEW_DEFINITION.getPortfolioId(), VIEW_DEFINITION.getMarketDataUser());
    newStoredViewWithOriginalName.setUniqueId(null);
    assertEquals(newStoredViewWithOriginalName, originalView);
    // view key without uid
    originalStoredViewKey = ViewKey.of(originalStoredView.getName());
    newStoredViewKey = configManager.createTemplateView(originalStoredViewKey, PORTFOLIO_KEY, VIEW_PREFIX);
    assertEquals(newStoredViewKey.getName(), VIEW_PREFIX + originalStoredView.getName());
    assertTrue(newStoredViewKey.hasUniqueId());
    assertEquals(configSource.getLatestByName(ViewDefinition.class, originalStoredView.getName()), originalStoredView);
    newStoredView = configSource.getConfig(ViewDefinition.class, newStoredViewKey.getUniqueId());
    assertEquals(newStoredView.getName(), VIEW_PREFIX + originalStoredView.getName());
    // reset name and uid and check everything else is the same
    newStoredViewWithOriginalName = newStoredView.copyWith(VIEW_DEFINITION.getName(), VIEW_DEFINITION.getPortfolioId(), VIEW_DEFINITION.getMarketDataUser());
    newStoredViewWithOriginalName.setUniqueId(null);
    assertEquals(newStoredViewWithOriginalName, originalView);
  }

  /**
   * Tests delete with a view key that has a uid set.
   */
  @Test
  public void testDeleteView1() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    final ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<>();
    request.setType(ViewDefinition.class);
    // config master should not contain any view definitions
    ConfigSearchResult<ViewDefinition> result = configMaster.search(request);
    assertTrue(result.getDocuments().isEmpty());
    final ViewDefinition originalView = VIEW_DEFINITION.copyWith(VIEW_DEFINITION.getName(), VIEW_DEFINITION.getPortfolioId(), VIEW_DEFINITION.getMarketDataUser());
    final ViewDefinition originalStoredView = ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(originalView)).getValue();
    final ViewKey viewKey = ViewKey.of(originalStoredView);
    result = configMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    configManager.deleteView(viewKey);
    result = configMaster.search(request);
    assertTrue(result.getDocuments().isEmpty());
  }

  /**
   * Tests delete with a view key that does not have a uid set.
   */
  @Test
  public void testDeleteView2() {
    final ConfigMaster configMaster = new InMemoryConfigMaster();
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final ConfigManager configManager = new ConfigManager(configMaster, configSource);
    final ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<>();
    request.setType(ViewDefinition.class);
    // config master should not contain any view definitions
    ConfigSearchResult<ViewDefinition> result = configMaster.search(request);
    assertTrue(result.getDocuments().isEmpty());
    final ViewDefinition originalView = VIEW_DEFINITION.copyWith(VIEW_DEFINITION.getName(), VIEW_DEFINITION.getPortfolioId(), VIEW_DEFINITION.getMarketDataUser());
    final ViewDefinition originalStoredView = ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(originalView)).getValue();
    final ViewKey viewKey = ViewKey.of(originalStoredView.getName());
    result = configMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    configManager.deleteView(viewKey);
    result = configMaster.search(request);
    assertTrue(result.getDocuments().isEmpty());
  }
}
