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
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link CurveConfigurationForExposureValidator}.
 */
public class CurveConfigurationForExposureValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> VALIDATOR =
      CurveConfigurationForExposureValidator.getInstance();

  /**
   * Tests the behaviour when the exposure functions configuration is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExposureFunctionsConfiguration() {
    VALIDATOR.validate(null, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the version correction is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrection() {
    final ExposureFunctions config = new ExposureFunctions("Exposures", Collections.<String>emptyList(), Collections.<ExternalId, String>emptyMap());
    VALIDATOR.validate(config, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final ExposureFunctions config = new ExposureFunctions("Exposures", Collections.<String>emptyList(), Collections.<ExternalId, String>emptyMap());
    VALIDATOR.validate(config, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests the behaviour when the referenced curve construction configurations are not available from the source.
   */
  @Test
  public void testMissingConfigurations() {
    final Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "ABC"), "CCC 1");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "DEF"), "CCC 2");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "GHI"), "CCC 3");
    final ExposureFunctions config = new ExposureFunctions("Exposures", Collections.singletonList(CurrencyExposureFunction.NAME), idsToNames);
    // no configs available from source
    final Map<String, Class<?>> expectedMissing = new HashMap<>();
    expectedMissing.put("CCC 1", CurveConstructionConfiguration.class);
    expectedMissing.put("CCC 2", CurveConstructionConfiguration.class);
    expectedMissing.put("CCC 3", CurveConstructionConfiguration.class);
    ConfigurationValidationInfo<CurveConstructionConfiguration> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
    // one config available from source
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0,
        Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve 1", Arrays.asList(new DiscountingCurveTypeConfiguration("ABC"))));
    final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration("CCC 1", Collections.singletonList(group), Collections.<String>emptyList());
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc, ccc.getName(), CurveConstructionConfiguration.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    expectedMissing.clear();
    expectedMissing.put("CCC 2", CurveConstructionConfiguration.class);
    expectedMissing.put("CCC 3", CurveConstructionConfiguration.class);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(ccc));
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
  }

  /**
   * Tests that duplicated curve construction configurations, i.e. multiple configurations with the same name present in the source, are identified.
   */
  @Test
  public void testDuplicatedConfigurations() {
    final Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "ABC"), "CCC 1");
    final ExposureFunctions config = new ExposureFunctions("Exposures", Collections.singletonList(CurrencyExposureFunction.NAME), idsToNames);
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0,
        Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap("Curve 1", Arrays.asList(new DiscountingCurveTypeConfiguration("ABC"))));
    final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration("CCC 1", Collections.singletonList(group), Collections.<String>emptyList());
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc, ccc.getName(), CurveConstructionConfiguration.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc, ccc.getName(), CurveConstructionConfiguration.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final Collection<ConfigItem<CurveConstructionConfiguration>> items =
        configSource.get(CurveConstructionConfiguration.class, ccc.getName(), VersionCorrection.LATEST);
    final ConfigurationValidationInfo<CurveConstructionConfiguration> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getDuplicatedConfigurations(), items);
  }

}
