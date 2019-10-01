/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.config.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DummyWebResource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Tests for {@link RemoteConfigSource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteConfigSourceTest {
  private URI _baseUri;

  /**
   * Creates the base URI.
   *
   * @throws URISyntaxException
   *           if there is a problem with the path
   */
  @BeforeMethod
  public void createBaseUri() throws URISyntaxException {
    _baseUri = new URI("path/to");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri11() {
    new RemoteConfigSource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteConfigSource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteConfigSource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteConfigSource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteConfigSource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("cfg", "1");
    final ConfigItem<?> item = new ConfigItem<>("config item");
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(uid, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.get(uid), item);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteConfigSource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteConfigSource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final ConfigItem<?> item = new ConfigItem<>("config item");
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.get(oid, version), item);
  }

  /**
   * Tests that the wrong expected class throws an exception.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetByClassUidWrongType() {
    final UniqueId uid = UniqueId.of("cfg", "1");
    final String config = "config item";
    final ConfigItem<?> item = new ConfigItem<>(config);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(uid, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    configSource.getConfig(ConfigItem.class, uid);
  }

  /**
   * Tests getting by class and unique id.
   */
  public void testGetByClassUid() {
    final UniqueId uid = UniqueId.of("cfg", "1");
    final String config = "config item";
    final ConfigItem<?> item = new ConfigItem<>(config);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(uid, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.getConfig(String.class, uid), config);
  }

  /**
   * Tests that the wrong expected class throws an exception.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetByClassOidVersionWrongType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final ConfigItem<?> item = new ConfigItem<>("config item");
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    configSource.getConfig(ConfigItem.class, oid, version);
  }

  /**
   * Tests getting by class and unique id.
   */
  public void testGetByClassOidVersion() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final String value = "config item";
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final ConfigItem<?> item = new ConfigItem<>(value);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.getConfig(String.class, oid, version), value);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullType() {
    new RemoteConfigSource(_baseUri).getSingle(null, "name", VersionCorrection.LATEST);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullName() {
    new RemoteConfigSource(_baseUri).getSingle(String.class, null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion() {
    new RemoteConfigSource(_baseUri).getSingle(String.class, "name", null);
  }

  /**
   * Tests that the type must match the expected type.
   */
  public void testGetSingleWrongType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final String name = "name";
    final ConfigItem<?> item = new ConfigItem<>("config item");
    item.setName(name);
    item.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    configSource.getSingle(ConfigItem.class, name, version);
  }

  /**
   * Tests getting a single config by name and type.
   */
  public void testGetSingleNameType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final String name = "name";
    final String value = "config item";
    final ConfigItem<?> item = new ConfigItem<>(value);
    item.setName(name);
    item.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.getSingle(String.class, name, version), value);
  }

  /**
   * Tests that the type must match the expected type.
   */
  public void testGetWrongType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final String name = "name";
    final ConfigItem<?> item = new ConfigItem<>("config item");
    item.setName(name);
    item.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    configSource.get(ConfigItem.class, name, version);
  }

  /**
   * Tests getting all configs by name and type.
   */
  public void testGetNameType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final String name = "name";
    final String value1 = "config item 1";
    final String value2 = "config item 2";
    final ConfigItem<?> item1 = new ConfigItem<>(value1);
    final ConfigItem<?> item2 = new ConfigItem<>(value2);
    item1.setName(name);
    item1.setType(String.class);
    item2.setName(name);
    item2.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item1);
    resource.addData(oid, version, item2);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEqualsNoOrder(configSource.get(String.class, name, version), Arrays.asList(item1, item2));
  }

  /**
   * Tests getting the latest config by name and type.
   */
  public void testGetLatestByNameType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final String name = "name";
    final String value = "config item";
    final ConfigItem<?> item = new ConfigItem<>(value);
    item.setName(name);
    item.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, VersionCorrection.LATEST, item);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(configSource.getLatestByName(String.class, name), value);
  }

  /**
   * Tests getting all configs of a type.
   */
  public void testGetAllType() {
    final ObjectId oid = ObjectId.of("cfg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final String value1 = "config item 1";
    final String value2 = "config item 2";
    final String value3 = "config item 3";
    final ConfigItem<?> item1 = new ConfigItem<>(value1);
    final ConfigItem<?> item2 = new ConfigItem<>(value2);
    final ConfigItem<?> item3 = new ConfigItem<>(value3);
    item1.setType(String.class);
    item2.setType(String.class);
    item3.setType(String.class);
    final DummyConfigWebResource resource = new DummyConfigWebResource();
    resource.addData(oid, version, item1);
    resource.addData(oid, version, item2);
    resource.addData(oid, version, item3);
    final RemoteConfigSource configSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEqualsNoOrder(configSource.getAll(String.class, version), Arrays.asList(item1, item2, item3));
  }

  private static class DummyRemoteSource<TYPE> extends RemoteConfigSource {
    private final DummyWebResource<TYPE> _resource;

    DummyRemoteSource(final URI baseUri, final DummyWebResource<TYPE> resource) {
      super(baseUri);
      _resource = resource;
    }

    DummyRemoteSource(final URI baseUri, final ChangeManager changeManager, final DummyWebResource<TYPE> resource) {
      super(baseUri, changeManager);
      _resource = resource;
    }

    @Override
    public UniformInterface accessRemote(final URI uri) {
      _resource.addURI(getBaseUri(), uri);
      return _resource;
    }
  }

  private static class DummyConfigWebResource extends DummyWebResource<ConfigItem<?>> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
      final String query = getUri().getQuery();
      final String path = getUri().getPath();
      final String appended = path.replace(getBaseUri().getPath(), "");
      if (query == null) {
        // object id or unique id
        final String[] components = appended.split("/");
        final UniqueId uid;
        if (components.length == 3) { // unique id
          uid = UniqueId.parse(components[2]);
        } else if (components.length == 4) { // object id
          uid = UniqueId.of(ObjectId.parse(components[2]), components[3]);
        } else {
          throw new IllegalStateException(path);
        }
        final List<ConfigItem<?>> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      } else {
        final String[] keyValues = query.split("&");
        String name = null, versionCorrection = null;
        for (final String keyValue : keyValues) {
          final String[] kv = keyValue.split("=");
          if (kv[0].equals("name")) {
            name = kv[1];
          } else if (kv[0].equals("versionCorrection")) {
            versionCorrection = kv[1];
          }
        }
        if (path.contains("single")) {
          for (final Map.Entry<UniqueId, List<ConfigItem<?>>> entry : getDataByUniqueId().entrySet()) {
            final List<ConfigItem<?>> items = entry.getValue();
            for (final ConfigItem<?> item : items) {
              if (item.getName() != null && item.getName().equals(name) && entry.getKey().getVersion().equals(versionCorrection)) {
                return (T) item.getValue();
              }
            }
          }
        } else if (path.contains("Searches")) {
          final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
          final MutableFudgeMsg msg = serializer.newMessage();
          for (final Map.Entry<UniqueId, List<ConfigItem<?>>> entry : getDataByUniqueId().entrySet()) {
            for (final ConfigItem<?> item : entry.getValue()) {
              if (entry.getKey().getVersion().equals(versionCorrection)) {
                serializer.addToMessage(msg, null, null, item);
              }
            }
          }
          return (T) msg;
        } else {
          final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
          final MutableFudgeMsg msg = serializer.newMessage();
          for (final Map.Entry<UniqueId, List<ConfigItem<?>>> entry : getDataByUniqueId().entrySet()) {
            for (final ConfigItem<?> item : entry.getValue()) {
              if (item.getName() != null && item.getName().equals(name) && entry.getKey().getVersion().equals(versionCorrection)) {
                serializer.addToMessage(msg, null, null, item);
              }
            }
          }
          return (T) msg;
        }
      }
      throw new DataNotFoundException("");
    }
  }
}
