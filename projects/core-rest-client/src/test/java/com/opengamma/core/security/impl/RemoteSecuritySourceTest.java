/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.security.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
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
import com.opengamma.core.security.Security;
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
 * Tests for {@link RemoteSecuritySource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteSecuritySourceTest {
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
    new RemoteSecuritySource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteSecuritySource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteSecuritySource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteSecuritySource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteSecuritySource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("sec", "1");
    final Security security = new TestSecurity(uid, "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(uid, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.get(uid), security);
  }

  // /**
  // * Tests that the unique id cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullUniqueId() {
  // new RemoteSecuritySource(_baseUri).get((UniqueId) null,
  // TestSecurity.class);
  // }
  //
  // /**
  // * Tests that the type cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullType() {
  // new RemoteSecuritySource(_baseUri).get(UniqueId.of("sec", "1"), null);
  // }
  //
  // /**
  // * Tests getting by unique id.
  // */
  // public void testGetByUidType() {
  // final UniqueId uid = UniqueId.of("sec", "1");
  // final Security security = new TestSecurity(uid, "security");
  // final DummySecurityWebResource resource = new DummySecurityWebResource();
  // resource.addData(uid, security);
  // final RemoteSecuritySource securitySource = new
  // DummyRemoteSource<>(_baseUri, resource);
  // assertEquals(securitySource.get(uid, TestSecurity.class), security);
  // }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteSecuritySource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteSecuritySource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("sec", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Security security = new TestSecurity(UniqueId.of(oid, version.toString()), "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(oid, version, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.get(oid, version), security);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle1() {
    new RemoteSecuritySource(_baseUri).get((ExternalIdBundle) null);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final Security security = new TestSecurity(null, "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eids, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.get(eids), Collections.singleton(security));
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle2() {
    new RemoteSecuritySource(_baseUri).get((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests getting by external id bundle and version.
   */
  public void testGetByExternalIdBundleVersion() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final VersionCorrection version = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
    final Security security = new TestSecurity(null, "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eids, version, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.get(eids, version), Collections.singleton(security));
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the unique ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUidCollection() {
    new RemoteSecuritySource(_baseUri).get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting by collection of unique ids.
   */
  public void testGetUidCollection() {
    final List<UniqueId> uids = Arrays.asList(UniqueId.of("sec", "1"), UniqueId.of("sec", "2"));
    final Security security1 = new TestSecurity(uids.get(0), "one");
    final Security security2 = new TestSecurity(uids.get(1), "two");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(uids.get(0), security1);
    resource.addData(uids.get(1), security2);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<UniqueId, Security> expected = new HashMap<>();
    expected.put(uids.get(0), security1);
    expected.put(uids.get(1), security2);
    assertEquals(securitySource.get(uids), expected);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds1() {
    new RemoteSecuritySource(_baseUri).getSingle((ExternalIdBundle) null);
  }

  /**
   * Tests getting a security by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("sec", "1");
    final Security security = new TestSecurity(UniqueId.of("sec", "1", null), "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eid, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.getSingle(eid), security);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds2() {
    new RemoteSecuritySource(_baseUri).getSingle((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion1() {
    new RemoteSecuritySource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("sec", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting a security by external id bundles.
   */
  public void testGetSingleExternalIdBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("sec", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Security security = new TestSecurity(UniqueId.of("sec", "1", null), "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eid, version, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(securitySource.getSingle(eid, version), security);
  }

  /**
   * Tests that the external id bundles cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullIds() {
    new RemoteSecuritySource(_baseUri).getAll((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullVersion() {
    new RemoteSecuritySource(_baseUri).getAll(Collections.singleton(ExternalIdBundle.of("sec", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting securities by external id bundle.
   */
  public void testGetAllVersion() {
    final ExternalIdBundle eid1 = ExternalIdBundle.of("sec", "1");
    final ExternalIdBundle eid2 = ExternalIdBundle.of("sec", "2");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Security security1 = new TestSecurity(UniqueId.of("sec", "1", null), "security");
    final Security security2 = new TestSecurity(UniqueId.of("sec", "2", null), "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eid1, version, security1);
    resource.addData(eid2, version, security2);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, Collection<Security>> expected = new HashMap<>();
    expected.put(eid1, Collections.singleton(security1));
    expected.put(eid2, Collections.singleton(security2));
    assertEqualsNoOrder(securitySource.getAll(Arrays.asList(eid1, eid2), version), expected);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds3() {
    new RemoteSecuritySource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion2() {
    new RemoteSecuritySource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("sec", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting a security by external id bundle.
   */
  public void testGetSingleExternalIdCollectionBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("sec", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Security security = new TestSecurity(UniqueId.of("sec", "1", null), "security");
    final DummySecurityWebResource resource = new DummySecurityWebResource();
    resource.addData(eid, version, security);
    final RemoteSecuritySource securitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, Security> expected = new HashMap<>();
    expected.put(eid, security);
    assertEquals(securitySource.getSingle(Collections.singleton(eid), version), expected);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteSecuritySource {
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

  private static class DummySecurityWebResource extends DummyWebResource<Security> {

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
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Security>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                return (T) entry.getValue().get(0);
              }
            }
          } else if (path.contains("bulk")) {
            final List<Security> securitys = new ArrayList<>();
            for (final Map.Entry<UniqueId, List<Security>> entry : getDataByUniqueId().entrySet()) {
              for (final ExternalId eid : ids) {
                final UniqueId uid = UniqueId.of(eid.getScheme().getName(), eid.getValue());
                if (entry.getKey().equals(uid)) {
                  securitys.addAll(entry.getValue());
                }
              }
            }
            return (T) FudgeListWrapper.of(securitys);
          } else {
            final List<Security> securitys = new ArrayList<>();
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Security>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                securitys.addAll(entry.getValue());
              }
            }
            return (T) FudgeListWrapper.of(securitys);
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
        final List<Security> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      }
      throw new DataNotFoundException("");
    }
  }

  private static class TestSecurity implements Security {
    private final UniqueId _uid;
    private final String _name;

    TestSecurity(final UniqueId uid, final String name) {
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
    public String getName() {
      return _name;
    }

    @Override
    public String getSecurityType() {
      return null;
    }

  }
}
