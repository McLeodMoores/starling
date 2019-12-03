/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataConventionSourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataConventionSourceResourceTest {
  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private ConventionSource _underlying;
  private UriInfo _uriInfo;
  private DataConventionSourceResource _resource;

  /**
   * Sets up a convention source.
   */
  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConventionSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConventionSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a configuration by unique id.
   */
  @Test
  public void testGetConventionByUid() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    when(_underlying.get(eq(UID))).thenReturn(target);
    final Response test = _resource.get(OID.toString(), UID.getVersion(), "", "");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  /**
   * Tests getting a convention by object id.
   */
  @Test
  public void testGetConventionByOid() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    when(_underlying.get(eq(OID), eq(VC))).thenReturn(target);
    final Response test = _resource.get(OID.toString(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  /**
   * Tests searching for a convention by identifiers and version.
   */
  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void testSearchIdsVersion() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    final Collection targetColl = ImmutableList.<Convention>of(target);
    when(_underlying.get(eq(BUNDLE), eq(VC))).thenReturn(targetColl);
    final Response test = _resource.search(VC.getVersionAsOfString(), VC.getCorrectedToString(), BUNDLE.toStringList());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl), test.getEntity());
  }

  /**
   * Tests getting conventions by identifiers.
   */
  @Test
  public void testGetBulk() {
    final MockConvention target1 = new MockConvention("TEST 1");
    final MockConvention target2 = new MockConvention("TEST 2");
    final List<UniqueId> uids = Arrays.asList(UniqueId.of("uid", "1"), UniqueId.of("uid", "2"));
    final Map<UniqueId, Convention> targetColl = ImmutableMap.<UniqueId, Convention> of(UniqueId.of("uid", "1"), target1, UniqueId.of("uid", "2"), target2);
    when(_underlying.get(uids)).thenReturn(targetColl);
    final Response test = _resource.getBulk(Arrays.asList("uid~1", "uid~2"));
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl.values()), test.getEntity());
  }

  /**
   * Tests getting conventions by identifiers.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testConventionSearches() {
    final MockConvention target1 = new MockConvention("TEST 1");
    final MockConvention target2 = new MockConvention("TEST 2");
    final List<String> idStrings = Arrays.asList("eid~1", "eid~2");
    final Collection<Convention> targetColl = Arrays.<Convention> asList(target1, target2);
    when(_underlying.get(ExternalIdBundle.parse(idStrings))).thenReturn(targetColl);
    final Response test = _resource.searchList(idStrings);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl), test.getEntity());
  }

  /**
   * Tests getting conventions by identifiers.
   */
  @Test
  public void testSearchSingleId() {
    final MockConvention target = new MockConvention("TEST 1");
    final List<String> idStrings = Arrays.asList("eid~1", "eid~2");
    when(_underlying.getSingle(eq(ExternalIdBundle.parse(idStrings)), any(VersionCorrection.class))).thenReturn(target);
    final Response test = _resource.searchSingle(idStrings, null, null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(target, test.getEntity());
  }

  /**
   * Tests getting conventions by identifiers.
   */
  @Test
  public void testSearchSingleIdType() {
    final MockConvention target = new MockConvention("TEST 1");
    final List<String> idStrings = Arrays.asList("eid~1", "eid~2");
    when(_underlying.getSingle(eq(ExternalIdBundle.parse(idStrings)), any(VersionCorrection.class), eq(MockConvention.class))).thenReturn(target);
    final Response test = _resource.searchSingle(idStrings, null, null, MockConvention.class.getName());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(target, test.getEntity());
  }

}
