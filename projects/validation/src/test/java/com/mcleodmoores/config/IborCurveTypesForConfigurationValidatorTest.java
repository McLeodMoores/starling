/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.mcleodmoores.config.IborCurveTypesForConfigurationValidator.IborCurveInformation;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link IborCurveTypesForConfigurationValidator}.
 */
public class IborCurveTypesForConfigurationValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<CurveConstructionConfiguration, IborCurveInformation> VALIDATOR =
      IborCurveTypesForConfigurationValidator.getInstance();

  /**
   * Tests the behaviour when the curve construction configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveGroupConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.singletonList(group), Collections.<String>emptyList());
    VALIDATOR.validate(config, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.singletonList(group), Collections.<String>emptyList());
    VALIDATOR.validate(config, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests that other curve type configurations are not validated.
   */
  @Test
  public void testOvernightCurveType() {
    final List<? extends CurveTypeConfiguration> type = Collections.singletonList(new OvernightCurveTypeConfiguration(ExternalId.of("CONVENTION", "Test")));
    final CurveGroupConfiguration group =
        new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve", type));
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.singletonList(group), Collections.<String>emptyList());
    final ConfigurationValidationInfo<IborCurveInformation> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
  }

  /**
   * Tests that duplicated references are found.
   */
  @Test
  public void testDuplicatedReferences() {
    final ExternalId reference1 = ExternalId.of("CONVENTION", "LIBOR");
    final ExternalId reference2 = ExternalId.of("CONVENTION", "EURIBOR");
    final List<CurveTypeConfiguration> type1 = new ArrayList<>();
    type1.add(new IborCurveTypeConfiguration(reference1, Tenor.THREE_MONTHS));
    final List<CurveTypeConfiguration> type2 = new ArrayList<>();
    type2.add(new IborCurveTypeConfiguration(reference1, Tenor.THREE_MONTHS));
    final List<CurveTypeConfiguration> type3 = new ArrayList<>();
    type3.add(new IborCurveTypeConfiguration(reference1, Tenor.THREE_MONTHS));
    // test with duplicated currencies in the same group
    Map<String, List<? extends CurveTypeConfiguration>> types = new HashMap<>();
    types.put("Curve 1", type1);
    types.put("Curve 2", type2);
    types.put("Curve 3", type3);
    CurveGroupConfiguration group = new CurveGroupConfiguration(0, types);
    CurveConstructionConfiguration config = new CurveConstructionConfiguration("name", Collections.singletonList(group), Collections.<String>emptyList());
    ConfigurationValidationInfo<IborCurveInformation> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
    // test with duplicated currencies in different groups
    Map<String, List<? extends CurveTypeConfiguration>> types1 = new HashMap<>();
    types1.put("Curve 1", type1);
    Map<String, List<? extends CurveTypeConfiguration>> types2 = new HashMap<>();
    types2.put("Curve 2", type2);
    Map<String, List<? extends CurveTypeConfiguration>> types3 = new HashMap<>();
    types3.put("Curve 3", type3);
    CurveGroupConfiguration group1 = new CurveGroupConfiguration(0, types1);
    CurveGroupConfiguration group2 = new CurveGroupConfiguration(1, types2);
    CurveGroupConfiguration group3 = new CurveGroupConfiguration(2, types3);
    config = new CurveConstructionConfiguration("name", Arrays.asList(group1, group2, group3), Collections.<String>emptyList());
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
    // one currency not duplicated
    types = new HashMap<>();
    types.put("Curve 1", type1);
    types.put("Curve 2", type2);
    types.put("Curve 3", Collections.singletonList(new IborCurveTypeConfiguration(reference2, Tenor.THREE_MONTHS)));
    group = new CurveGroupConfiguration(0, types);
    config = new CurveConstructionConfiguration("name", Collections.singletonList(group), Collections.<String>emptyList());
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(new IborCurveInformation(reference2, Tenor.THREE_MONTHS)));
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
    types1 = new HashMap<>();
    types1.put("Curve 1", type1);
    types2 = new HashMap<>();
    types2.put("Curve 2", type2);
    types3 = new HashMap<>();
    types3.put("Curve 3", Collections.singletonList(new IborCurveTypeConfiguration(reference2, Tenor.THREE_MONTHS)));
    group1 = new CurveGroupConfiguration(0, types1);
    group2 = new CurveGroupConfiguration(1, types2);
    group3 = new CurveGroupConfiguration(2, types3);
    config = new CurveConstructionConfiguration("name", Arrays.asList(group1, group2, group3), Collections.<String>emptyList());
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(new IborCurveInformation(reference2, Tenor.THREE_MONTHS)));
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
  }

  /**
   * Tests that duplicated references in exogenous configurations are found.
   */
  @Test
  public void testDuplicatedReferencesExogenousConfiguration() {
    final ExternalId reference1 = ExternalId.of("CONVENTION", "LIBOR");
    final ExternalId reference2 = ExternalId.of("CONVENTION", "EURIBOR");
    final ExternalId reference3 = ExternalId.of("CONVENTION", "CDOR");
    final List<CurveTypeConfiguration> type1 = new ArrayList<>();
    type1.add(new IborCurveTypeConfiguration(reference1, Tenor.THREE_MONTHS));
    final List<CurveTypeConfiguration> type2 = new ArrayList<>();
    type2.add(new IborCurveTypeConfiguration(reference1, Tenor.THREE_MONTHS));
    final List<CurveTypeConfiguration> type3 = new ArrayList<>();
    type3.add(new IborCurveTypeConfiguration(reference2, Tenor.THREE_MONTHS));
    final List<CurveTypeConfiguration> type4 = new ArrayList<>();
    type4.add(new IborCurveTypeConfiguration(reference2, Tenor.THREE_MONTHS));
    final Map<String, List<? extends CurveTypeConfiguration>> types1 = new HashMap<>();
    types1.put("Curve 1", type1);
    types1.put("Curve 2", type2);
    final Map<String, List<? extends CurveTypeConfiguration>> types2 = new HashMap<>();
    types2.put("Curve 3", type3);
    final Map<String, List<? extends CurveTypeConfiguration>> types3 = new HashMap<>();
    types3.put("Curve 4", type4);
    // multiple exogenous configurations
    CurveGroupConfiguration group1 = new CurveGroupConfiguration(0, types1);
    CurveGroupConfiguration group2 = new CurveGroupConfiguration(0, types2);
    CurveGroupConfiguration group3 = new CurveGroupConfiguration(1, types3);
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name2", Arrays.asList(group2, group3),
        Collections.singletonList("name3")), "name2", CurveConstructionConfiguration.class)));
    CurveConstructionConfiguration config = new CurveConstructionConfiguration("name1", Collections.singletonList(group1), Arrays.asList("name2"));
    ConfigSource configSource = new MasterConfigSource(configMaster);
    ConfigurationValidationInfo<IborCurveInformation> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getDuplicatedConfigurations(), Sets.newHashSet(new IborCurveInformation(reference1, Tenor.THREE_MONTHS),
        new IborCurveInformation(reference2, Tenor.THREE_MONTHS)));
    // nested exogenous configurations
    group1 = new CurveGroupConfiguration(0, types1);
    group2 = new CurveGroupConfiguration(0, types2);
    group3 = new CurveGroupConfiguration(0, types3);
    configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name2", Collections.singletonList(group2),
        Collections.singletonList("name3")), "name2", CurveConstructionConfiguration.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name3", Collections.singletonList(group3),
        Collections.<String>emptyList()), "name3", CurveConstructionConfiguration.class)));
    config = new CurveConstructionConfiguration("name1", Collections.singletonList(group1), Arrays.asList("name2"));
    configSource = new MasterConfigSource(configMaster);
    // one currency duplicated, multiple exogenous configurations
    group1 = new CurveGroupConfiguration(0, types1);
    group2 = new CurveGroupConfiguration(0, types2);
    group3 = new CurveGroupConfiguration(1, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap(
        "Curve 5", Collections.singletonList(new IborCurveTypeConfiguration(reference3, Tenor.THREE_MONTHS))));
    configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name2", Arrays.asList(group2, group3),
        Collections.singletonList("name3")), "name2", CurveConstructionConfiguration.class)));
    config = new CurveConstructionConfiguration("name1", Collections.singletonList(group1), Arrays.asList("name2"));
    configSource = new MasterConfigSource(configMaster);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
    assertEqualsNoOrder(validationInfo.getValidatedConfigurations(), Sets.newHashSet(new IborCurveInformation(reference2, Tenor.THREE_MONTHS),
        new IborCurveInformation(reference3, Tenor.THREE_MONTHS)));
    // one currency duplicated, nested exogenous configurations
    group1 = new CurveGroupConfiguration(0, types1);
    group2 = new CurveGroupConfiguration(0, types2);
    group3 = new CurveGroupConfiguration(1, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap(
        "Curve 5", Collections.singletonList(new IborCurveTypeConfiguration(reference3, Tenor.THREE_MONTHS))));
    configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name2", Collections.singletonList(group2),
        Collections.singletonList("name3")), "name2", CurveConstructionConfiguration.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(new CurveConstructionConfiguration("name3", Collections.singletonList(group3),
        Collections.<String>emptyList()), "name3", CurveConstructionConfiguration.class)));
    config = new CurveConstructionConfiguration("name1", Collections.singletonList(group1), Arrays.asList("name2"));
    configSource = new MasterConfigSource(configMaster);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.singleton(new IborCurveInformation(reference1, Tenor.THREE_MONTHS)));
    assertEqualsNoOrder(validationInfo.getValidatedConfigurations(), Sets.newHashSet(new IborCurveInformation(reference2, Tenor.THREE_MONTHS),
        new IborCurveInformation(reference3, Tenor.THREE_MONTHS)));
  }
}
