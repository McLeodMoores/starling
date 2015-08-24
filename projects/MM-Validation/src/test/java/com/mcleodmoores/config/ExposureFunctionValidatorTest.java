/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.SecurityExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityTypeExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.factory.ExposureFunctionAdapter;
import com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * Unit tests for {@link ExposureFunctionValidator}.
 */
public class ExposureFunctionValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<ExposureFunctions, ExposureFunction> VALIDATOR = ExposureFunctionValidator.getInstance();

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
    final ExposureFunctions config = new ExposureFunctions("name", Collections.<String>emptyList(), Collections.<ExternalId, String>emptyMap());
    VALIDATOR.validate(config, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final ExposureFunctions config = new ExposureFunctions("name", Collections.<String>emptyList(), Collections.<ExternalId, String>emptyMap());
    VALIDATOR.validate(config, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests the behaviour when the exposure function could not be retrieved from the factory.
   */
  @Test
  public void testMissingFunction() {
    final String name1 = "Exposure function 1";
    final String name2 = "Exposure function 2";
    final String name3 = "Exposure function 3";
    List<String> functions = Arrays.asList(name1, name2, name3);
    ExposureFunctions config = new ExposureFunctions("name", functions, Collections.<ExternalId, String>emptyMap());
    // no functions available from factory
    final Map<String, Class<?>> expectedMissing = new HashMap<>();
    expectedMissing.put(name1, NamedExposureFunction.class);
    expectedMissing.put(name2, NamedExposureFunction.class);
    expectedMissing.put(name3, NamedExposureFunction.class);
    ConfigurationValidationInfo<ExposureFunction> validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
    // one function not available from factory
    functions = Arrays.asList(name1, SecurityExposureFunction.NAME, SecurityTypeExposureFunction.NAME);
    config = new ExposureFunctions("name", functions, Collections.<ExternalId, String>emptyMap());
    expectedMissing.clear();
    expectedMissing.put(name1, NamedExposureFunction.class);
    validationInfo = VALIDATOR.validate(config, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Sets.newHashSet(new ExposureFunctionAdapter(new SecurityExposureFunction()),
        new ExposureFunctionAdapter(new SecurityTypeExposureFunction())));
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
  }
}
