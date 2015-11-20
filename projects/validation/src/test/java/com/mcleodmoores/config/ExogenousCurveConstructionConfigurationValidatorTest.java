/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * Unit tests for {@link ExogenousCurveConstructionConfigurationValidator}.
 */
public class ExogenousCurveConstructionConfigurationValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> VALIDATOR =
      ExogenousCurveConstructionConfigurationValidator.getInstance();

  /**
   * Tests the behaviour when the curve construction configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveConstructionConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.<CurveGroupConfiguration>emptyList(),
        Arrays.asList("config"));
    VALIDATOR.validate(config, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.<CurveGroupConfiguration>emptyList(),
        Arrays.asList("config"));
    VALIDATOR.validate(config, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests that missing exogenous curve construction configurations are identified.
   */
  @Test
  public void testMissingExogenousConfigurations() {
    final String name1 = "name1";
    final String name2 = "name2";
    final String name3 = "name3";
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.<CurveGroupConfiguration>emptyList(),
        Arrays.asList(name1, name2, name3));
    // no config in source
    final Map<String, Class<?>> expectedMissing = new HashMap<>();
    expectedMissing.put(name1, CurveConstructionConfiguration.class);
    expectedMissing.put(name2, CurveConstructionConfiguration.class);
    expectedMissing.put(name3, CurveConstructionConfiguration.class);
    ConfigurationValidationInfo<CurveConstructionConfiguration> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
    // one config in source
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    final CurveConstructionConfiguration ccc1 = new CurveConstructionConfiguration(name1, Collections.<CurveGroupConfiguration>emptyList(),
        Collections.<String>emptyList());
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc1, ccc1.getName(), CurveConstructionConfiguration.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    expectedMissing.clear();
    expectedMissing.put(name2, CurveConstructionConfiguration.class);
    expectedMissing.put(name3, CurveConstructionConfiguration.class);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(ccc1));
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
  }

  /**
   * Tests that duplicated configurations, i.e. multiple curve construction configurations with the same name present in the source, are
   * identified.
   */
  @Test
  public void testDuplicatedExogenousConfigurations() {
    final String name = "name1";
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.<CurveGroupConfiguration>emptyList(),
        Arrays.asList(name));
    ConfigurationValidationInfo<CurveConstructionConfiguration> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration(name, Collections.<CurveGroupConfiguration>emptyList(),
        Collections.<String>emptyList());
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc, ccc.getName(), CurveConstructionConfiguration.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc, ccc.getName(), CurveConstructionConfiguration.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final Collection<ConfigItem<CurveConstructionConfiguration>> expectedDuplicated =
        configSource.get(CurveConstructionConfiguration.class, ccc.getName(), VersionCorrection.LATEST);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getDuplicatedConfigurations(), expectedDuplicated);
  }
}
