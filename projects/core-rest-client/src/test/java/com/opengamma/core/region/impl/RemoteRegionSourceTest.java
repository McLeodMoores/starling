/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region.impl;

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
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DummyWebResource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Tests for {@link RemoteRegionSource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteRegionSourceTest {
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
    new RemoteRegionSource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteRegionSource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteRegionSource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteRegionSource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteRegionSource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("reg", "1");
    final Region region = new TestRegion(uid, "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(uid, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(uid), region);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetTypeNullUniqueId() {
    new RemoteRegionSource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUidType() {
    final UniqueId uid = UniqueId.of("reg", "1");
    final Region region = new TestRegion(uid, "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(uid, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(uid), region);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteRegionSource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteRegionSource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("reg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Region region = new TestRegion(UniqueId.of(oid, version.toString()), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(oid, version, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(oid, version), region);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersionType() {
    final ObjectId oid = ObjectId.of("reg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Region region = new TestRegion(UniqueId.of(oid, version.toString()), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(oid, version, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(oid, version), region);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle1() {
    new RemoteRegionSource(_baseUri).get((ExternalIdBundle) null);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final Region region = new TestRegion(null, "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eids, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(eids), Collections.singleton(region));
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle2() {
    new RemoteRegionSource(_baseUri).get((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests getting by external id bundle and version.
   */
  public void testGetByExternalIdBundleVersion() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final VersionCorrection version = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
    final Region region = new TestRegion(null, "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eids, version, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.get(eids, version), Collections.singleton(region));
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the unique ids cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullUidCollection() {
    new RemoteRegionSource(_baseUri).get((Collection<UniqueId>) null);
  }

  /**
   * Tests getting by collection of unique ids.
   */
  public void testGetUidCollection() {
    final List<UniqueId> uids = Arrays.asList(UniqueId.of("reg", "1"), UniqueId.of("reg", "2"));
    final Region region1 = new TestRegion(uids.get(0), "one");
    final Region region2 = new TestRegion(uids.get(1), "two");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(uids.get(0), region1);
    resource.addData(uids.get(1), region2);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<UniqueId, Region> expected = new HashMap<>();
    expected.put(uids.get(0), region1);
    expected.put(uids.get(1), region2);
    assertEquals(regionSource.get(uids), expected);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds1() {
    new RemoteRegionSource(_baseUri).getSingle((ExternalIdBundle) null);
  }

  /**
   * Tests getting a region by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("reg", "1");
    final Region region = new TestRegion(UniqueId.of("reg", "1", null), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eid, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.getSingle(eid), region);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds3() {
    new RemoteRegionSource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion1() {
    new RemoteRegionSource(_baseUri).getSingle(ExternalIdBundle.of("reg", "1"), (VersionCorrection) null);
  }

  /**
   * Tests getting a region by external id bundle.
   */
  public void testGetSingleExternalIdBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("reg", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Region region = new TestRegion(UniqueId.of("reg", "1", null), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eid, version, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.getSingle(eid, version), region);
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetHighestLevelNullEid() {
    new RemoteRegionSource(_baseUri).getHighestLevelRegion((ExternalId) null);
  }

  /**
   * Tests getting the highest level region by external id.
   */
  public void testGetHighestLevelRegionExternalId() {
    final ExternalId eid = ExternalId.of("reg", "1");
    final Region region = new TestRegion(UniqueId.of("reg", "1", null), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eid, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.getHighestLevelRegion(eid), region);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetHighestLevelNullEids() {
    new RemoteRegionSource(_baseUri).getHighestLevelRegion((ExternalIdBundle) null);
  }

  /**
   * Tests getting the highest level region by external id bundle.
   */
  public void testGetHighestLevelRegionExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("reg", "1");
    final Region region = new TestRegion(UniqueId.of("reg", "1", null), "region");
    final DummyRegionWebResource resource = new DummyRegionWebResource();
    resource.addData(eid, region);
    final RemoteRegionSource regionSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(regionSource.getHighestLevelRegion(eid), region);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteRegionSource {
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

  private static class DummyRegionWebResource extends DummyWebResource<Region> {

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
          if (path.contains("highest")) {
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Region>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                return (T) entry.getValue().get(0);
              }
            }
          } else {
            final List<Region> regions = new ArrayList<>();
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Region>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                regions.addAll(entry.getValue());
              }
            }
            return (T) FudgeListWrapper.of(regions);
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
        final List<Region> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      }
      throw new DataNotFoundException("");
    }
  }

  private static class TestRegion implements Region {
    private final UniqueId _uid;
    private final String _name;

    public TestRegion(final UniqueId uid, final String name) {
      _uid = uid;
      _name = name;
    }

    @Override
    public UniqueId getUniqueId() {
      return _uid;
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
    public RegionClassification getClassification() {
      return null;
    }

    @Override
    public Set<UniqueId> getParentRegionIds() {
      return null;
    }

    @Override
    public Country getCountry() {
      return null;
    }

    @Override
    public Currency getCurrency() {
      return null;
    }

    @Override
    public ZoneId getTimeZone() {
      return null;
    }

    @Override
    public String getFullName() {
      return null;
    }

    @Override
    public FlexiBean getData() {
      return null;
    }

  }
}
