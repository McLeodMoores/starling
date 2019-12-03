/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.base.Throwables;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteDependencyGraphTraceProviderTest {

  private RemoteDependencyGraphTraceProvider _provider;
  private final String _baseUrl = "http://host.com/";

  @BeforeMethod
  public void beforeMethod() throws URISyntaxException {

    final URI uri = new URI(_baseUrl);
    _provider = new RemoteDependencyGraphTraceProvider(uri);

  }

  @Test
  public void getTraceDefaults() {

    final URI uri = _provider.buildUri(new DependencyGraphTraceBuilderProperties());

    final String uriStr = decode(uri);

    //assert default values are there
    assertTrue(uriStr.contains("calculationConfigurationName/Default"));
    assertTrue(uriStr.contains("defaultProperties/EMPTY"));
    assertTrue(uriStr.contains("resolutionTime/VLATEST.CLATEST"));

  }

  @Test
  public void getTraceCalculationConfigurationName() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    properties = properties.calculationConfigurationName("test");

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);

    assertTrue(uriStr.contains("calculationConfigurationName/test"));

  }

  @Test
  public void getTraceDefaultProperties() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String defaultPropertiesStr1 = "{A=[foo,bar],B=[*]}";
    final String defaultPropertiesStr2 = "{A=[bar,foo],B=[*]}";
    final ValueProperties parsed = ValueProperties.parse(defaultPropertiesStr1);

    properties = properties.defaultProperties(parsed);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("defaultProperties/" + defaultPropertiesStr1) || uriStr.contains("defaultProperties/" + defaultPropertiesStr2));

  }

  @Test
  public void getTraceMarketDataUser() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String snapshotId = "Foo~1";
    final UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));

    properties = properties.addMarketData(marketData);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("marketDataSnapshot/" + snapshotId));

  }

  @Test
  public void getTraceMarketDataLiveDefault() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final MarketDataSpecification marketData = MarketData.live();

    properties = properties.addMarketData(marketData);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("marketDataLiveDefault"));

  }

  @Test
  public void getTraceMarketDataLive() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final MarketDataSpecification marketData = MarketData.live("BB");

    properties = properties.addMarketData(marketData);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("marketDataLive/BB"));

  }

  @Test
  public void getTraceMarketDataHistorical() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
    final LocalDate now = LocalDate.now();
    final MarketDataSpecification marketData = MarketData.historical(now, "timeseries");

    properties = properties.addMarketData(marketData);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("marketDataHistorical/" + now.toString() + "/timeseries"));
    assertEquals(now, LocalDate.parse(now.toString()));

  }

  @Test
  public void getTraceResolutionTime() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String rtStr = "V1970-01-01T00:00:01Z.CLATEST";
    final VersionCorrection rt = VersionCorrection.parse(rtStr);

    properties = properties.resolutionTime(rt);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("resolutionTime/" + rtStr));

  }

  @Test
  public void getTraceValuationTime() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String instantStr = "2013-06-24T12:18:01.094Z";
    final Instant instant = Instant.parse(instantStr);

    properties = properties.valuationTime(instant);

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("valuationTime/" + instantStr));

  }

  @Test
  public void uriValueRequirementByExternalId() throws UnsupportedEncodingException {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String valueName = "test1";
    final String targetType = "POSITION";
    final String idStr = "GOLDMAN~Foo1";
    final ExternalId id = ExternalId.parse(idStr);

    properties = properties.addRequirement(new ValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.POSITION, id)));

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("requirement/" + valueName + "/" + targetType + "/" + idStr));

  }

  @Test
  public void uriValueRequirementByUniqueId() throws UnsupportedEncodingException {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();

    final String valueName = "test1";
    final String targetType = "POSITION";
    final String idStr = "GOLDMAN~Foo1";
    final UniqueId id = UniqueId.parse(idStr);

    properties = properties.addRequirement(new ValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.POSITION, id)));

    final URI uri = _provider.buildUri(properties);

    final String uriStr = decode(uri);
    assertTrue(uriStr.contains("value/" + valueName + "/" + targetType + "/" + idStr));

  }

  private String decode(final URI uriDefaultProperties) {
    final String urlStr = uriDefaultProperties.toString();
    try {
      return URLDecoder.decode(urlStr, "UTF-8");
    } catch (final UnsupportedEncodingException ex) {
      throw Throwables.propagate(ex);
    }
  }

}
