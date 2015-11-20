/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
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
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CurveGroupValidator}.
 */
public class CurveGroupValidatorTest {
  /** An empty config source */
  private static final ConfigSource EMPTY_CONFIG_SOURCE = new MasterConfigSource(new InMemoryConfigMaster());
  /** The validator */
  private static final ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> VALIDATOR = CurveGroupValidator.getInstance();

  /**
   * Tests the behaviour when the curve group configuration is null.
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
    VALIDATOR.validate(group, null, EMPTY_CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>emptyMap());
    VALIDATOR.validate(group, VersionCorrection.LATEST, (ConfigSource) null);
  }

  /**
   * Tests that missing curve definitions are identified.
   */
  @Test
  public void testMissingCurveDefinitions() {
    final String curveName1 = "Curve 1";
    final String curveName2 = "Curve 2";
    final DiscountingCurveTypeConfiguration type1 = new DiscountingCurveTypeConfiguration("USD");
    final IborCurveTypeConfiguration type2 = new IborCurveTypeConfiguration(ExternalId.of("CONVENTION", "IBOR"), Tenor.THREE_MONTHS);
    final OvernightCurveTypeConfiguration type3 = new OvernightCurveTypeConfiguration(ExternalId.of("CONVENTION", "OVERNIGHT"));
    final Map<String, List<? extends CurveTypeConfiguration>> types = new HashMap<>();
    types.put(curveName1, Arrays.asList(type1, type3));
    types.put(curveName2, Arrays.asList(type2));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, types);
    // no curves in source
    final Map<String, Class<?>> expectedMissing = new HashMap<>();
    expectedMissing.put(curveName1, AbstractCurveDefinition.class);
    expectedMissing.put(curveName2, AbstractCurveDefinition.class);
    ConfigurationValidationInfo<AbstractCurveDefinition> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, EMPTY_CONFIG_SOURCE);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
    // one curve in source
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    final ConstantCurveDefinition curveDefinition = new ConstantCurveDefinition(curveName1, ExternalId.of("TEST", "TEST"));
    configMaster.add(new ConfigDocument(ConfigItem.of(curveDefinition, curveName1, ConstantCurveDefinition.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    expectedMissing.clear();
    expectedMissing.put(curveName2, AbstractCurveDefinition.class);
    validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getDuplicatedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getUnsupportedConfigurations(), Collections.emptySet());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.singleton(curveDefinition));
    assertEquals(validationInfo.getMissingConfigurations(), expectedMissing);
  }

  /**
   * Tests that duplicated definitions, i.e. multiple curve definitions with the same name present in the source, are identified.
   */
  @Test
  public void testDuplicatedCurveDefinitions() {
    final String curveName = "Curve";
    final DiscountingCurveTypeConfiguration type = new DiscountingCurveTypeConfiguration("USD");
    final Map<String, List<? extends CurveTypeConfiguration>> types = new HashMap<>();
    types.put(curveName, Collections.singletonList(type));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, types);
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    final ConstantCurveDefinition curveDefinition = new ConstantCurveDefinition(curveName, ExternalId.of("TEST", "TEST"));
    configMaster.add(new ConfigDocument(ConfigItem.of(curveDefinition, curveName, ConstantCurveDefinition.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(curveDefinition, curveName, ConstantCurveDefinition.class)));
    configMaster.add(new ConfigDocument(ConfigItem.of(curveDefinition, curveName, ConstantCurveDefinition.class)));
    final ConfigSource configSource = new MasterConfigSource(configMaster);
    final Collection<ConfigItem<AbstractCurveDefinition>> expectedDuplicated =
        configSource.get(AbstractCurveDefinition.class, curveName, VersionCorrection.LATEST);
    final ConfigurationValidationInfo<AbstractCurveDefinition> validationInfo = VALIDATOR.validate(group, VersionCorrection.LATEST, configSource);
    assertEquals(validationInfo.getMissingConfigurations(), Collections.emptyMap());
    assertEquals(validationInfo.getValidatedConfigurations(), Collections.emptySet());
    assertEqualsNoOrder(validationInfo.getDuplicatedConfigurations(), expectedDuplicated);
  }

}
