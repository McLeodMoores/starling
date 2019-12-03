/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.exchange.impl;

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
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DummyWebResource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.exchange.Exchange;
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
 * Tests for {@link RemoteExchangeSource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteExchangeSourceTest {
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
    new RemoteExchangeSource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteExchangeSource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteExchangeSource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteExchangeSource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullUniqueId() {
    new RemoteExchangeSource(_baseUri).get((UniqueId) null);
  }

  /**
   * Tests getting by unique id.
   */
  public void testGetByUid() {
    final UniqueId uid = UniqueId.of("exch", "1");
    final Exchange exchange = new TestExchange(uid, "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(uid, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.get(uid), exchange);
  }

  // /**
  // * Tests that the unique id cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullUniqueId() {
  // new RemoteExchangeSource(_baseUri).get((UniqueId) null,
  // TestExchange.class);
  // }
  //
  // /**
  // * Tests that the type cannot be null.
  // */
  // @Test(expectedExceptions = IllegalArgumentException.class)
  // public void testGetTypeNullType() {
  // new RemoteExchangeSource(_baseUri).get(UniqueId.of("exch", "1"), null);
  // }
  //
  // /**
  // * Tests getting by unique id.
  // */
  // public void testGetByUidType() {
  // final UniqueId uid = UniqueId.of("exch", "1");
  // final Exchange exchange = new TestExchange(uid, "exchange");
  // final DummyExchangeWebResource resource = new
  // DummyExchangeWebResource();
  // resource.addData(uid, exchange);
  // final RemoteExchangeSource exchangeSource = new
  // DummyRemoteSource<>(_baseUri, resource);
  // assertEquals(exchangeSource.get(uid, TestExchange.class),
  // exchange);
  // }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullObjectId() {
    new RemoteExchangeSource(_baseUri).get((ObjectId) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNullVersionCorrection() {
    new RemoteExchangeSource(_baseUri).get(ObjectId.of("oid", "1"), null);
  }

  /**
   * Tests getting by object id and version.
   */
  public void testGetByObjectIdVersion() {
    final ObjectId oid = ObjectId.of("exch", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(10000));
    final Exchange exchange = new TestExchange(UniqueId.of(oid, version.toString()), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(oid, version, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.get(oid, version), exchange);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle1() {
    new RemoteExchangeSource(_baseUri).get((ExternalIdBundle) null);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final Exchange exchange = new TestExchange(null, "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eids, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.get(eids), Collections.singleton(exchange));
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetEidsNullBundle2() {
    new RemoteExchangeSource(_baseUri).get((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests getting by external id bundle and version.
   */
  public void testGetByExternalIdBundleVersion() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("abc", "1"), ExternalId.of("def", "2"));
    final VersionCorrection version = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
    final Exchange exchange = new TestExchange(null, "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eids, version, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.get(eids, version), Collections.singleton(exchange));
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds1() {
    new RemoteExchangeSource(_baseUri).getSingle((ExternalId) null);
  }

  /**
   * Tests the behaviour when there is no exchange for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoExchange1() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyExchangeWebResource()).getSingle(ExternalId.of("eid", "1")));
  }

  /**
   * Tests getting a exchange by external id.
   */
  public void testGetSingleExternalId() {
    final ExternalId eid = ExternalId.of("exch", "1");
    final Exchange exchange = new TestExchange(UniqueId.of("exch", "1", null), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eid, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.getSingle(eid), exchange);
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds2() {
    new RemoteExchangeSource(_baseUri).getSingle((ExternalIdBundle) null);
  }

  /**
   * Tests the behaviour when there is no exchange for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoExchange2() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyExchangeWebResource()).getSingle(ExternalIdBundle.of("eid", "1")));
  }

  /**
   * Tests getting a exchange by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("exch", "1");
    final Exchange exchange = new TestExchange(UniqueId.of("exch", "1", null), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eid, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.getSingle(eid), exchange);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds3() {
    new RemoteExchangeSource(_baseUri).getSingle((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion1() {
    new RemoteExchangeSource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("exch", "1")), (VersionCorrection) null);
  }

  /**
   * Tests the exception when there is no exchange for an id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSingleNoExchange3() {
    assertNull(new DummyRemoteSource<>(_baseUri, new DummyExchangeWebResource()).getSingle(ExternalIdBundle.of("eid", "1"), VersionCorrection.LATEST));
  }

  /**
   * Tests getting a exchange by external id bundles.
   */
  public void testGetSingleExternalIdBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("exch", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Exchange exchange = new TestExchange(UniqueId.of("exch", "1", null), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eid, version, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(exchangeSource.getSingle(eid, version), exchange);
  }

  /**
   * Tests that the external id bundles cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullIds() {
    new RemoteExchangeSource(_baseUri).getAll((Collection<ExternalIdBundle>) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAllNullVersion() {
    new RemoteExchangeSource(_baseUri).getAll(Collections.singleton(ExternalIdBundle.of("exch", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting securities by external id bundle.
   */
  public void testGetAllVersion() {
    final ExternalIdBundle eid1 = ExternalIdBundle.of("exch", "1");
    final ExternalIdBundle eid2 = ExternalIdBundle.of("exch", "2");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Exchange exchange1 = new TestExchange(UniqueId.of("exch", "1", null), "exchange");
    final Exchange exchange2 = new TestExchange(UniqueId.of("exch", "2", null), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eid1, version, exchange1);
    resource.addData(eid2, version, exchange2);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, Collection<Exchange>> expected = new HashMap<>();
    expected.put(eid1, Collections.singleton(exchange1));
    expected.put(eid2, Collections.singleton(exchange2));
    assertEqualsNoOrder(exchangeSource.getAll(Arrays.asList(eid1, eid2), version), expected);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullIds4() {
    new RemoteExchangeSource(_baseUri).getSingle((ExternalIdBundle) null, VersionCorrection.LATEST);
  }

  /**
   * Tests that the version cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSingleNullVersion2() {
    new RemoteExchangeSource(_baseUri).getSingle(Collections.singleton(ExternalIdBundle.of("exch", "1")), (VersionCorrection) null);
  }

  /**
   * Tests getting a exchange by external id bundle.
   */
  public void testGetSingleExternalIdCollectionBundleVersion() {
    final ExternalIdBundle eid = ExternalIdBundle.of("exch", "1");
    final VersionCorrection version = VersionCorrection.ofVersionAsOf(Instant.ofEpochSecond(100000));
    final Exchange exchange = new TestExchange(UniqueId.of("exch", "1", null), "exchange");
    final DummyExchangeWebResource resource = new DummyExchangeWebResource();
    resource.addData(eid, version, exchange);
    final RemoteExchangeSource exchangeSource = new DummyRemoteSource<>(_baseUri, resource);
    final Map<ExternalIdBundle, Exchange> expected = new HashMap<>();
    expected.put(eid, exchange);
    assertEquals(exchangeSource.getSingle(Collections.singleton(eid), version), expected);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteExchangeSource {
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

  private static class DummyExchangeWebResource extends DummyWebResource<Exchange> {

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
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Exchange>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                return (T) entry.getValue().get(0);
              }
            }
          } else if (path.contains("bulk")) {
            final List<Exchange> exchanges = new ArrayList<>();
            for (final Map.Entry<UniqueId, List<Exchange>> entry : getDataByUniqueId().entrySet()) {
              for (final ExternalId eid : ids) {
                final UniqueId uid = UniqueId.of(eid.getScheme().getName(), eid.getValue());
                if (entry.getKey().equals(uid)) {
                  exchanges.addAll(entry.getValue());
                }
              }
            }
            return (T) FudgeListWrapper.of(exchanges);
          } else {
            final List<Exchange> exchanges = new ArrayList<>();
            for (final Map.Entry<Pair<ExternalIdBundle, String>, List<Exchange>> entry : getDataByExternalIdBundle().entrySet()) {
              if (entry.getKey().getFirst().containsAll(eids) && entry.getKey().getSecond().equals(versionCorrection)) {
                exchanges.addAll(entry.getValue());
              }
            }
            return (T) FudgeListWrapper.of(exchanges);
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
        final List<Exchange> result = getDataByUniqueId().get(uid);
        if (result != null) {
          return (T) result.get(0);
        }
      }
      throw new DataNotFoundException("");
    }
  }

  private static class TestExchange implements Exchange {
    private final UniqueId _uid;
    private final String _name;

    public TestExchange(final UniqueId uid, final String name) {
      _uid = uid;
      _name = name;
    }

    @Override
    public UniqueId getUniqueId() {
      return _uid;
    }

    @Override
    public String getName() {
      return _name;
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return null;
    }

    @Override
    public ExternalIdBundle getRegionIdBundle() {
      return null;
    }

    @Override
    public ZoneId getTimeZone() {
      return null;
    }

  }
}
