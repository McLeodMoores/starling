/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.DateSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataConfigSourceResourceTest {
  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final String NAME = "name";
  private ConfigSource _underlying;
  private UriInfo _uriInfo;
  private DataConfigSourceResource _resource;

  /**
   * Sets up a config source.
   */
  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConfigSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConfigSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a configuration by unique id.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test
  public void testGetConfigByUid() {
    final DateSet target = DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1))));
    final ConfigItem<?> item = ConfigItem.of(target);
    item.setName("Test");
    when(_underlying.get(eq(UID))).thenReturn((ConfigItem) item);
    final Response test = _resource.get(UID.toString());
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) test.getEntity()).getValue(), target);
  }

  /**
   * Tests getting a configuration by object id.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test
  public void testGetConfigByOid() {
    final DateSet target = DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1))));
    final ConfigItem<?> item = ConfigItem.of(target);
    item.setName("Test");
    when(_underlying.get(eq(OID), eq(VC))).thenReturn((ConfigItem) ConfigItem.of(target));
    final Response test = _resource.getByOidVersionCorrection(OID.toString(), VC.toString());
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) test.getEntity()).getValue(), target);
  }

  /**
   * Tests searching for a configuration by name, type and version.
   */
  @Test
  public void testSearchByNameTypeVersion() {
    final ConfigItem<DateSet> config1 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1)))));
    config1.setName(NAME);
    final ConfigItem<DateSet> config2 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 2), LocalDate.of(2018, 1, 2)))));
    config2.setName("other name");
    when(_underlying.get(eq(DateSet.class), eq(NAME), eq(VC))).thenReturn(Collections.singleton(config1));
    when(_underlying.get(eq(DateSet.class), eq("other name"), eq(VC))).thenReturn(Collections.singleton(config2));
    final Response test = _resource.search(DateSet.class.getName(), VC.toString(), NAME);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 1);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()), config1);
  }

  /**
   * Tests searching for a configuration by name and type.
   */
  @Test
  public void testSearchByNameType() {
    final ConfigItem<DateSet> config1 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1)))));
    config1.setName(NAME);
    final ConfigItem<DateSet> config2 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 2), LocalDate.of(2018, 1, 2)))));
    config2.setName("other name");
    when(_underlying.get(eq(DateSet.class), eq(NAME), eq(VersionCorrection.LATEST))).thenReturn(Collections.singleton(config1));
    when(_underlying.get(eq(DateSet.class), eq("other name"), eq(VersionCorrection.LATEST))).thenReturn(Collections.singleton(config2));
    final Response test = _resource.search(DateSet.class.getName(), null, NAME);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 1);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()), config1);
  }

  /**
   * Tests searching for a configuration by type.
   */
  @Test
  public void testSearchByType() {
    final ConfigItem<DateSet> config1 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1)))));
    config1.setName(NAME);
    final ConfigItem<DateSet> config2 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2017, 1, 2), LocalDate.of(2018, 1, 2)))));
    config2.setName("other name");
    when(_underlying.getAll(eq(DateSet.class), any(VersionCorrection.class))).thenReturn(Arrays.asList(config1, config2));
    final Response test = _resource.search(DateSet.class.getName(), null, null);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 2);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()), config1);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(1).getValue()), config2);
  }

  /**
   * Tests searching for a single configuration by type and version.
   */
  @Test
  public void testSearchSingleByTypeVersion() {
    final ConfigItem<LocalDate> config1 = ConfigItem.of(LocalDate.of(2018, 1, 1));
    config1.setName(NAME);
    final ConfigItem<LocalDate> config2 = ConfigItem.of(LocalDate.of(2019, 1, 1));
    config2.setName(NAME);
    when(_underlying.getSingle(eq(LocalDate.class), eq(NAME), eq(VC))).thenReturn(config2.getValue());
    final Response test = _resource.searchSingle(LocalDate.class.getName(), VC.toString(), NAME);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 1);
    assertEquals(msg.getAllFields().get(0).getValue(), config2.getValue());
  }

  /**
   * Tests searching for a single configuration by type.
   */
  @Test
  public void testSearchSingleByType() {
    final ConfigItem<LocalDate> config1 = ConfigItem.of(LocalDate.of(2018, 1, 1));
    config1.setName(NAME);
    final ConfigItem<LocalDate> config2 = ConfigItem.of(LocalDate.of(2019, 1, 1));
    config2.setName(NAME);
    when(_underlying.getSingle(eq(LocalDate.class), eq(NAME), any(VersionCorrection.class))).thenReturn(config2.getValue());
    final Response test = _resource.searchSingle(LocalDate.class.getName(), null, NAME);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 1);
    assertEquals(msg.getAllFields().get(0).getValue(), config2.getValue());
  }

  /**
   * Tests searching for configurations by type and version.
   */
  @Test
  public void testConfigSearchesByTypeVersion() {
    final ConfigItem<LocalDate> config1 = ConfigItem.of(LocalDate.of(2018, 1, 1));
    config1.setName(NAME);
    final ConfigItem<LocalDate> config2 = ConfigItem.of(LocalDate.of(2019, 1, 1));
    config2.setName("other name");
    when(_underlying.getAll(eq(LocalDate.class), eq(VC))).thenReturn(Arrays.asList(config1, config2));
    final Response test = _resource.search(LocalDate.class.getName(), VC.toString());
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 2);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()), config1);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(1).getValue()), config2);
  }

  /**
   * Tests searching for configurations by type.
   */
  @Test
  public void testConfigSearchesByType() {
    final ConfigItem<LocalDate> config1 = ConfigItem.of(LocalDate.of(2018, 1, 1));
    config1.setName(NAME);
    final ConfigItem<LocalDate> config2 = ConfigItem.of(LocalDate.of(2019, 1, 1));
    config2.setName("other name");
    when(_underlying.getAll(eq(LocalDate.class), any(VersionCorrection.class))).thenReturn(Arrays.asList(config1, config2));
    final Response test = _resource.search(LocalDate.class.getName(), null);
    assertEquals(test.getStatus(), Status.OK.getStatusCode());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(msg.getNumFields(), 2);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()), config1);
    assertEquals(OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(1).getValue()), config2);
  }
}
