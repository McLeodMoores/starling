/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity.impl;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
import com.opengamma.core.legalentity.Account;
import com.opengamma.core.legalentity.Capability;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.Obligation;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.RootPortfolio;
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
 * Tests for {@link RemoteLegalEntitySource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteLegalEntitySourceTest {
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
    new RemoteLegalEntitySource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteLegalEntitySource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteLegalEntitySource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteLegalEntitySource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteLegalEntitySource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("len", "1");
    final LegalEntity legalEntity = new TestLegalEntity(uid, "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(uid, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.get(uid), legalEntity);
  }

  // /**
  // * Tests that the unique id cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullUniqueId() {
  // new RemoteLegalEntitySource(_baseUri).get((UniqueId) null,
  // TestLegalEntity.class);
  // }
  //
  // /**
  // * Tests that the type cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullType() {
  // new RemoteLegalEntitySource(_baseUri).get(UniqueId.of("len", "1"), null);
  // }
  //
  // /**
  // * Tests getting by unique id.
  // */
  // public void testGetByUidType() {
  // final UniqueId uid = UniqueId.of("len", "1");
  // final LegalEntity legalEntity = new TestLegalEntity(uid, "legalEntity");
  // final DummyLegalEntityWebResource resource = new
  // DummyLegalEntityWebResource();
  // resource.addData(uid, legalEntity);
  // final RemoteLegalEntitySource legalEntitySource = new
  // DummyRemoteSource<>(_baseUri, resource);
  // assertEquals(legalEntitySource.get(uid, TestLegalEntity.class),
  // legalEntity);
  // }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteLegalEntitySource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteLegalEntitySource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("len", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final LegalEntity legalEntity = new TestLegalEntity(UniqueId.of(oid, version.toString()), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(oid, version, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.get(oid, version), legalEntity);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle1() {
    new RemoteLegalEntitySource(_baseUri).get((ExternalIdBundle) null);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final LegalEntity legalEntity = new TestLegalEntity(null, "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eids, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.get(eids), Collections.singleton(legalEntity));
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle2() {
    new RemoteLegalEntitySource(_baseUri).get((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests getting by external id bundle and version.
   */
  public void testGetByExternalIdBundleVersion() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final VersionCorrection version = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
    final LegalEntity legalEntity = new TestLegalEntity(null, "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eids, version, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.get(eids, version), Collections.singleton(legalEntity));
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the unique ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUidCollection() {
    new RemoteLegalEntitySource(_baseUri).get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting by collection of unique ids.
   */
  public void testGetUidCollection() {
    final List<UniqueId> uids = Arrays.asList(UniqueId.of("len", "1"), UniqueId.of("len", "2"));
    final LegalEntity legalEntity1 = new TestLegalEntity(uids.get(0), "one");
    final LegalEntity legalEntity2 = new TestLegalEntity(uids.get(1), "two");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(uids.get(0), legalEntity1);
    resource.addData(uids.get(1), legalEntity2);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<UniqueId, LegalEntity> expected = new HashMap<>();
    expected.put(uids.get(0), legalEntity1);
    expected.put(uids.get(1), legalEntity2);
    assertEquals(legalEntitySource.get(uids), expected);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds1() {
    new RemoteLegalEntitySource(_baseUri).getSingle((ExternalId) null);
  }

  /**
   * Tests the behaviour when there is no legal entity for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoLegalEntity1() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyLegalEntityWebResource()).getSingle(ExternalId.of("eid", "1")));
  }

  /**
   * Tests getting a legal entity by external id.
   */
  public void testGetSingleExternalId() {
    final ExternalId eid = ExternalId.of("len", "1");
    final LegalEntity legalEntity = new TestLegalEntity(UniqueId.of("len", "1", null), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eid, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.getSingle(eid), legalEntity);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds2() {
    new RemoteLegalEntitySource(_baseUri).getSingle((ExternalIdBundle) null);
  }

  /**
   * Tests the behaviour when there is no legal entity for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoLegalEntity2() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyLegalEntityWebResource()).getSingle(ExternalIdBundle.of("eid", "1")));
  }

  /**
   * Tests getting a legal entity by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("len", "1");
    final LegalEntity legalEntity = new TestLegalEntity(UniqueId.of("len", "1", null), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eid, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.getSingle(eid), legalEntity);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds3() {
    new RemoteLegalEntitySource(_baseUri).getSingle((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion1() {
    new RemoteLegalEntitySource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("len", "1")), (VersionCorrection) null);
  }

  /**
   * Tests the exception when there is no legal entity for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoLegalEntity3() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyLegalEntityWebResource()).getSingle(ExternalIdBundle.of("eid", "1"), VersionCorrection.LATEST));
  }

  /**
   * Tests getting a legal entity by external id bundles.
   */
  public void testGetSingleExternalIdBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("len", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final LegalEntity legalEntity = new TestLegalEntity(UniqueId.of("len", "1", null), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eid, version, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(legalEntitySource.getSingle(eid, version), legalEntity);
  }

  /**
   * Tests that the external id bundles cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullIds() {
    new RemoteLegalEntitySource(_baseUri).getAll((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullVersion() {
    new RemoteLegalEntitySource(_baseUri).getAll(Collections.singleton(ExternalIdBundle.of("len", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting securities by external id bundle.
   */
  public void testGetAllVersion() {
    final ExternalIdBundle eid1 = ExternalIdBundle.of("len", "1");
    final ExternalIdBundle eid2 = ExternalIdBundle.of("len", "2");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final LegalEntity legalEntity1 = new TestLegalEntity(UniqueId.of("len", "1", null), "legalEntity");
    final LegalEntity legalEntity2 = new TestLegalEntity(UniqueId.of("len", "2", null), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eid1, version, legalEntity1);
    resource.addData(eid2, version, legalEntity2);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, Collection<LegalEntity>> expected = new HashMap<>();
    expected.put(eid1, Collections.singleton(legalEntity1));
    expected.put(eid2, Collections.singleton(legalEntity2));
    assertEqualsNoOrder(legalEntitySource.getAll(Arrays.asList(eid1, eid2), version), expected);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds4() {
    new RemoteLegalEntitySource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion2() {
    new RemoteLegalEntitySource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("len", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting a legal entity by external id bundle.
   */
  public void testGetSingleExternalIdCollectionBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("len", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final LegalEntity legalEntity = new TestLegalEntity(UniqueId.of("len", "1", null), "legalEntity");
    final DummyLegalEntityWebResource resource = new DummyLegalEntityWebResource();
    resource.addData(eid, version, legalEntity);
    final RemoteLegalEntitySource legalEntitySource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, LegalEntity> expected = new HashMap<>();
    expected.put(eid, legalEntity);
    assertEquals(legalEntitySource.getSingle(Collections.singleton(eid), version), expected);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteLegalEntitySource {
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

  private static class DummyLegalEntityWebResource extends DummyWebResource<LegalEntity> {

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
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<LegalEntity>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                return (T) entry.getValue().get(0);
              }
            }
          } else if (path.contains("bulk")) {
            final List<LegalEntity> legalEntitys = new ArrayList<>();
            for (final Map.Entry<UniqueId, List<LegalEntity>> entry : getDataByUniqueId().entrySet()) {
              for (final ExternalId eid : ids) {
                final UniqueId uid = UniqueId.of(eid.getScheme().getName(), eid.getValue());
                if (entry.getKey().equals(uid)) {
                  legalEntitys.addAll(entry.getValue());
                }
              }
            }
            return (T) FudgeListWrapper.of(legalEntitys);
          } else {
            final List<LegalEntity> legalEntitys = new ArrayList<>();
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<LegalEntity>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                legalEntitys.addAll(entry.getValue());
              }
            }
            return (T) FudgeListWrapper.of(legalEntitys);
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
        final List<LegalEntity> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      }
      throw new DataNotFoundException("");
    }
  }

  private static class TestLegalEntity implements LegalEntity {
    private final UniqueId _uid;
    private final String _name;

    TestLegalEntity(final UniqueId uid, final String name) {
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
    public List<Rating> getRatings() {
      return null;
    }

    @Override
    public List<Capability> getCapabilities() {
      return null;
    }

    @Override
    public List<ExternalIdBundle> getIssuedSecurities() {
      return null;
    }

    @Override
    public List<Obligation> getObligations() {
      return null;
    }

    @Override
    public List<Account> getAccounts() {
      return null;
    }

    @Override
    public RootPortfolio getRootPortfolio() {
      return null;
    }

    @Override
    public Map<String, String> getDetails() {
      return null;
    }

    @Override
    public void setDetails(final Map<String, String> details) {

    }

    @Override
    public void addDetail(final String key, final String value) {
    }

  }
}
