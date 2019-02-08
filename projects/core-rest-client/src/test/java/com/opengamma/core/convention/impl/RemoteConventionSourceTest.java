/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DummyWebResource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Tests for {@link RemoteConventionSource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteConventionSourceTest {
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
    new RemoteConventionSource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteConventionSource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteConventionSource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteConventionSource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteConventionSource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("conv", "1");
    final Convention convention = new TestConvention(uid, "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(uid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(uid), convention);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTypeNullUniqueId() {
    new RemoteConventionSource(_baseUri).get((UniqueId) null, TestConvention.class);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTypeNullType() {
    new RemoteConventionSource(_baseUri).get(UniqueId.of("conv", "1"), null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUidType() {
    final UniqueId uid = UniqueId.of("conv", "1");
    final Convention convention = new TestConvention(uid, "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(uid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(uid, TestConvention.class), convention);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteConventionSource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteConventionSource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("conv", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Convention convention = new TestConvention(UniqueId.of(oid, version.toString()), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(oid, version, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(oid, version), convention);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTypeNullObjectId() {
    new RemoteConventionSource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST, TestConvention.class);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTypeNullVersionCorrection() {
    new RemoteConventionSource(_baseUri).get(ObjectId.of("oid", "1"), null, TestConvention.class);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetOidTypeNullType() {
    new RemoteConventionSource(_baseUri).get(ObjectId.of("oid", "1"), VersionCorrection.LATEST, null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersionType() {
    final ObjectId oid = ObjectId.of("conv", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Convention convention = new TestConvention(UniqueId.of(oid, version.toString()), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(oid, version, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(oid, version, TestConvention.class), convention);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle1() {
    new RemoteConventionSource(_baseUri).get((ExternalIdBundle) null);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final Convention convention = new TestConvention(null, "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eids, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(eids), Collections.singleton(convention));
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle2() {
    new RemoteConventionSource(_baseUri).get((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests getting by external id bundle and version.
   */
  public void testGetByExternalIdBundleVersion() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final VersionCorrection version = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
    final Convention convention = new TestConvention(null, "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eids, version, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.get(eids, version), Collections.singleton(convention));
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the unique ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUidCollection() {
    new RemoteConventionSource(_baseUri).get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting by collection of unique ids.
   */
  public void testGetUidCollection() {
    final List<UniqueId> uids = Arrays.asList(UniqueId.of("conv", "1"), UniqueId.of("conv", "2"));
    final Convention convention1 = new TestConvention(uids.get(0), "one");
    final Convention convention2 = new TestConvention(uids.get(1), "two");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(uids.get(0), convention1);
    resource.addData(uids.get(1), convention2);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<UniqueId, Convention> expected = new HashMap<>();
    expected.put(uids.get(0), convention1);
    expected.put(uids.get(1), convention2);
    assertEquals(conventionSource.get(uids), expected);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullId1() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalId) null);
  }

  /**
   * Tests getting a convention by external id.
   */
  public void testGetSingleExternalId() {
    final ExternalId eid = ExternalId.of("conv", "1");
    final Convention convention = new TestConvention(UniqueId.of(eid.getScheme().getName(), eid.getValue(), null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid), convention);
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullId2() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalId) null, TestConvention.class);
  }

  /**
   * Tests getting a convention by external id and type.
   */
  public void testGetSingleExternalIdType() {
    final ExternalId eid = ExternalId.of("conv", "1");
    final Convention convention = new TestConvention(UniqueId.of(eid.getScheme().getName(), eid.getValue(), null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid, TestConvention.class), convention);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds1() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalIdBundle) null);
  }

  /**
   * Tests getting a convention by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("conv", "1");
    final Convention convention = new TestConvention(UniqueId.of("conv", "1", null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid), convention);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds2() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalIdBundle) null, TestConvention.class);
  }

  /**
   * Tests getting a convention by external id bundle.
   */
  public void testGetSingleExternalIdBundleType() {
    final ExternalIdBundle eid = ExternalIdBundle.of("conv", "1");
    final Convention convention = new TestConvention(UniqueId.of("conv", "1", null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid, TestConvention.class), convention);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds3() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion1() {
    new RemoteConventionSource(_baseUri).getSingle(ExternalIdBundle.of("conv", "1"), (VersionCorrection) null);
  }

  /**
   * Tests getting a convention by external id bundle.
   */
  public void testGetSingleExternalIdBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("conv", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Convention convention = new TestConvention(UniqueId.of("conv", "1", null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, version, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid, version), convention);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds4() {
    new RemoteConventionSource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST, TestConvention.class);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion2() {
    new RemoteConventionSource(_baseUri).getSingle(ExternalIdBundle.of("conv", "1"), (VersionCorrection) null, TestConvention.class);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullType() {
    new RemoteConventionSource(_baseUri).getSingle(ExternalIdBundle.of("conv", "1"), VersionCorrection.LATEST, null);
  }

  /**
   * Tests getting a convention by external id bundle.
   */
  public void testGetSingleExternalIdBundleVersionType() {
    final ExternalIdBundle eid = ExternalIdBundle.of("conv", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Convention convention = new TestConvention(UniqueId.of("conv", "1", null), "convention");
    final DummyConventionWebResource resource = new DummyConventionWebResource();
    resource.addData(eid, version, convention);
    final RemoteConventionSource conventionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(conventionSource.getSingle(eid, version, TestConvention.class), convention);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteConventionSource {
    private final DummyWebResource<TYPE> _resource;

    public DummyRemoteSource(final URI baseUri, final DummyWebResource<TYPE> resource) {
      super(baseUri);
      _resource = resource;
    }

    public DummyRemoteSource(final URI baseUri, final ChangeManager changeManager, final DummyWebResource<TYPE> resource) {
      super(baseUri, changeManager);
      _resource = resource;
    }

    @Override
    public UniformInterface accessRemote(final URI uri) {
      _resource.addURI(getBaseUri(), uri);
      return _resource;
    }
  }

  private static class DummyConventionWebResource extends DummyWebResource<Convention> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
      final String query = getUri().getQuery();
      final String path = getUri().getPath();
      final String appended = path.replace(getBaseUri().getPath(), "");
      if (query != null && query.contains("id=")) {
        final String[] keyValues = query.split("&");
        final List<ExternalId> ids = new ArrayList<>();
        String versionCorrection = "";
        for (final String keyValue : keyValues) {
          final String[] kv = keyValue.split("=");
          if (kv[0].equalsIgnoreCase("id")) {
            ids.add(ExternalId.parse(kv[1]));
          } else if (kv[0].equalsIgnoreCase("versionAsOf")) {
            versionCorrection += "V" + kv[1];
          } else if (kv[0].equalsIgnoreCase("correctedTo")) {
            versionCorrection += ".C" + kv[1];
          }
        }
        if (!ids.isEmpty()) {
          final ExternalIdBundle eids = ExternalIdBundle.of(ids);
          if (versionCorrection.isEmpty()) {
            versionCorrection = VersionCorrection.LATEST.toString();
          }
          if (path.contains("single")) {
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Convention>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                return (T) entry.getValue().get(0);
              }
            }
          } else if (path.contains("bulk")) {
            final List<Convention> conventions = new ArrayList<>();
            for (final Map.Entry<UniqueId, List<Convention>> entry : getDataByUniqueId().entrySet()) {
              for (final ExternalId eid : ids) {
                final UniqueId uid = UniqueId.of(eid.getScheme().getName(), eid.getValue());
                if (entry.getKey().equals(uid)) {
                  conventions.addAll(entry.getValue());
                }
              }
            }
            return (T) FudgeListWrapper.of(conventions);
          } else {
            final List<Convention> conventions = new ArrayList<>();
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Convention>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                conventions.addAll(entry.getValue());
              }
            }
            return (T) FudgeListWrapper.of(conventions);
          }
        }
      } else {
        // object id or unique id
        final String[] components = appended.split("/");
        UniqueId uid = null;
        if (query != null) {
          final String[] queries = query.split("&");
          if (query.contains("correctedTo")) {
            final VersionCorrection vc = VersionCorrection.parse(queries[0].split("=")[1], queries[1].split("=")[1]);
            uid = UniqueId.parse(components[2]).withVersion(vc.toString());
          }
        } else {
          uid = UniqueId.parse(components[2]);
        }
        final List<Convention> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      }
      throw new DataNotFoundException("");
    }
  }

  private static class TestConvention implements Convention {
    private final UniqueId _uid;
    private final String _name;

    public TestConvention(final UniqueId uid, final String name) {
      _uid = uid;
      _name = name;
    }

    @Override
    public UniqueId getUniqueId() {
      return _uid;
    }

    @Override
    public Map<String, String> getAttributes() {
      return null;
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void addAttribute(final String key, final String value) {
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return null;
    }

    @Override
    public ConventionType getConventionType() {
      return ConventionType.of("test");
    }

    @Override
    public String getName() {
      return _name;
    }

  }
}
