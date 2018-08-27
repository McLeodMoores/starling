/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.provider.DependencyGraphTraceProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link DependencyGraphTraceProviderUris}
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphTraceProviderResourceTest {

  private static final String TEST_URL = "http://testurl.com/";

  private DependencyGraphTraceProviderResource _resource;
  private FudgeContext _fudgeContext;
  private DependencyGraphTraceProvider _provider;
  private DependencyGraphBuildTrace _sampleResult;
  private URI _baseUri;

  @BeforeMethod
  public void beforeTest() {
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
    _provider = mock(DependencyGraphTraceProvider.class);
    _sampleResult = DependencyGraphBuildTrace.of(null, null, null, null);
    _resource = new DependencyGraphTraceProviderResource(_provider, _fudgeContext);
    try {
      _baseUri = new URI(TEST_URL);
    } catch (final URISyntaxException ex) {
      Throwables.propagate(ex);
    }
  }

  @Test
  public void DependencyGraphTraceProviderUris() {
    assertEquals(_provider, _resource.getProvider());
    assertEquals(_fudgeContext, _resource.getFudgeContext());
  }

  @Test
  public void getTraceWithCalculationConfigurationName() {
    final String calcConfigName = "test";
    final String originalName = _resource.getProperties().getCalculationConfigurationName();

    final DependencyGraphTraceProviderResource newResource = _resource.setCalculationConfigurationName(calcConfigName);

    assertEquals(originalName, _resource.getProperties().getCalculationConfigurationName());
    assertEquals(calcConfigName, newResource.getProperties().getCalculationConfigurationName());

  }

  @Test
  public void getTraceWithDefaultProperties() {
    //input
    final String defaultProperties = "A=[foo,bar],B=*";

    //expected arg
    final ValueProperties props = ValueProperties.parse(defaultProperties);
    final ValueProperties originalProps = _resource.getProperties().getDefaultProperties();

    final DependencyGraphTraceProviderResource newResource = _resource.setDefaultProperties(defaultProperties);

    assertEquals(props, newResource.getProperties().getDefaultProperties());
    assertEquals(originalProps, _resource.getProperties().getDefaultProperties());

  }

  @Test
  public void getTraceWithMarketData() {
    //input
    final String snapshotId = "Foo~1";

    //expected arg
    final List<MarketDataSpecification> marketData = Lists.<MarketDataSpecification>newArrayList(MarketData.user(UniqueId.parse(snapshotId)));
    final List<MarketDataSpecification> originalMD = _resource.getProperties().getMarketData();

    final DependencyGraphTraceProviderResource newResource = _resource.setMarketDataSnapshot(snapshotId);

    assertEquals(marketData, newResource.getProperties().getMarketData());
    assertEquals(originalMD, _resource.getProperties().getMarketData());
  }

  @Test
  public void getTraceWithResolutionTime() {
    //input
    final String resolutionTime = "V1970-01-01T00:00:01Z.CLATEST";

    //expected arg
    final VersionCorrection parsed = VersionCorrection.parse(resolutionTime);
    final VersionCorrection originalRT = _resource.getProperties().getResolutionTime();

    final DependencyGraphTraceProviderResource newResource = _resource.setResolutionTime(resolutionTime);

    assertEquals(parsed, newResource.getProperties().getResolutionTime());
    assertEquals(originalRT, _resource.getProperties().getResolutionTime());
  }

  @Test
  public void getTraceWithValuationTime() {
    //input
    final String valuationTimeStr = "2013-06-24T12:18:01.094Z";

    //expected arg
    final Instant valuationTime = Instant.parse(valuationTimeStr);
    final Instant originalVT = _resource.getProperties().getValuationTime();

    final DependencyGraphTraceProviderResource newResource = _resource.setValuationTime(valuationTimeStr);

    assertEquals(valuationTime, newResource.getProperties().getValuationTime());
    assertEquals(originalVT, _resource.getProperties().getValuationTime());

  }

  @Test
  public void getTraceWithValueRequirementByExternalId() {
    //input
    final String valueName = "name";
    final String targetType = "POSITION";
    final String externalId = "Foo~1";

    //expected arg
    final ComputationTargetType expectedTargetType = ComputationTargetType.POSITION;
    final ExternalId expectedExternalId = ExternalId.parse(externalId);
    final ValueRequirement valueRequirement = new ValueRequirement(valueName, new ComputationTargetRequirement(expectedTargetType, expectedExternalId));

    final DependencyGraphTraceProviderResource newResource = _resource.setValueRequirementByExternalId(valueName, targetType, externalId);

    assertTrue(newResource.getProperties().getRequirements().contains(valueRequirement));

  }

  @Test
  public void getTraceWithValueRequirementByUniqueId() {
    //input
    final String valueName = "name";
    final String targetType = "POSITION";
    final String uniqueId = "Foo~1";

    //expected arg
    final UniqueId expectedUniqueId = UniqueId.parse(uniqueId);
    final ComputationTargetType expectedTargetType = ComputationTargetType.POSITION;
    final ValueRequirement valueRequirement = new ValueRequirement(valueName, new ComputationTargetSpecification(expectedTargetType, expectedUniqueId));

    final DependencyGraphTraceProviderResource newResource = _resource.setValueRequirementByUniqueId(valueName, targetType, uniqueId);

    assertTrue(newResource.getProperties().getRequirements().contains(valueRequirement));
  }

  //-----------------------------------------------------------

  @Test
  public void build() {

    when(_provider.getTrace(_resource.getProperties())).thenReturn(_sampleResult);

    final FudgeMsgEnvelope result = _resource.build();

    verify(_provider).getTrace(_resource.getProperties());
    assertNotNull(result);
  }

  //-----------------------------------------------------------

  @Test
  public void uriCalculationConfigurationName() throws UnsupportedEncodingException {
    final String testStr = "test";
    final URI uriCalculationConfigurationName = DependencyGraphTraceProviderUris.uriCalculationConfigurationName(_baseUri, testStr);
    final String url = decode(uriCalculationConfigurationName);
    assertEquals(TEST_URL + "calculationConfigurationName/" + testStr, url);
  }

  @Test
  public void uriDefaultProperties() throws UnsupportedEncodingException {
    final String defaultPropertiesStr1 = "{A=[foo,bar],B=[*]}";
    final String defaultPropertiesStr2 = "{A=[bar,foo],B=[*]}";
    final ValueProperties parsed = ValueProperties.parse(defaultPropertiesStr1);
    final URI uri = DependencyGraphTraceProviderUris.uriDefaultProperties(_baseUri, parsed);
    final String url = decode(uri);
    assertTrue(url.equals(TEST_URL + "defaultProperties/" + defaultPropertiesStr1) ||
        url.equals(TEST_URL + "defaultProperties/" + defaultPropertiesStr2));
  }

  @Test
  public void uriMarketDataSnapshot() throws UnsupportedEncodingException {
    final String snapshotId = "Foo~1";
    final MarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    final URI uri = DependencyGraphTraceProviderUris.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    final String url = decode(uri);
    assertEquals(TEST_URL + "marketDataSnapshot/" + snapshotId, url);
  }
  @Test
  public void uriMarketDataLiveDefault() throws UnsupportedEncodingException {
    final MarketDataSpecification marketData = MarketData.live();
    final URI uri = DependencyGraphTraceProviderUris.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    final String url = decode(uri);
    assertEquals(TEST_URL + "marketDataLiveDefault", url);
  }

  @Test
  public void uriMarketDataLive() throws UnsupportedEncodingException {
    final MarketDataSpecification marketData = MarketData.live("BB");
    final URI uri = DependencyGraphTraceProviderUris.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    final String url = decode(uri);
    assertEquals(TEST_URL + "marketDataLive/BB", url);
  }
  @Test
  public void uriMarketDataHistorical() throws UnsupportedEncodingException {
    final LocalDate now = LocalDate.now();
    final MarketDataSpecification marketData = MarketData.historical(now, "ts");
    final URI uri = DependencyGraphTraceProviderUris.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    final String url = decode(uri);
    assertEquals(TEST_URL + "marketDataHistorical/" + now + "/ts", url);
  }
  @Test
  public void uriResolutionTime() throws UnsupportedEncodingException {
    final String rtStr = "V1970-01-01T00:00:01Z.CLATEST";
    final VersionCorrection rt = VersionCorrection.parse(rtStr);
    final URI uri = DependencyGraphTraceProviderUris.uriResolutionTime(_baseUri, rt);
    final String url = decode(uri);
    assertEquals(TEST_URL + "resolutionTime/" + rtStr, url);
  }

  @Test
  public void uriValuationTime() throws UnsupportedEncodingException {
    final String instantStr = "2013-06-24T12:18:01.094Z";
    final Instant instant = Instant.parse(instantStr);
    final URI uri = DependencyGraphTraceProviderUris.uriValuationTime(_baseUri, instant);
    final String url = decode(uri);
    assertEquals(TEST_URL + "valuationTime/" + instantStr, url);
  }

  @Test
  public void uriValueRequirementByExternalId() throws UnsupportedEncodingException {
    final String valueName = "test1";
    final String targetType = "test2";
    final String idStr = "GOLDMAN~Foo1";
    final ExternalId id = ExternalId.parse(idStr);
    final URI uri = DependencyGraphTraceProviderUris.uriValueRequirementByExternalId(_baseUri, valueName, targetType, id);
    final String url = decode(uri);
    assertEquals(TEST_URL + "requirement/" + valueName + "/" + targetType + "/" + idStr, url);
  }

  @Test
  public void uriValueRequirementByUniqueId() throws UnsupportedEncodingException {
    final String valueName = "test1";
    final String targetType = "test2";
    final String idStr = "GOLDMAN~Foo1";
    final UniqueId id = UniqueId.parse(idStr);
    final URI uri = DependencyGraphTraceProviderUris.uriValueRequirementByUniqueId(_baseUri, valueName, targetType, id);
    final String url = decode(uri);
    assertEquals(TEST_URL + "value/" + valueName + "/" + targetType + "/" + idStr, url);
  }

  private String decode(final URI uriDefaultProperties) throws UnsupportedEncodingException {
    final String urlStr = uriDefaultProperties.toString();
    final String decoded = URLDecoder.decode(urlStr, "UTF-8");
    return decoded;
  }

}
