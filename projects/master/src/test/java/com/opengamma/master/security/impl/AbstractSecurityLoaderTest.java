/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link AbstractSecurityLoader}.
 */
@Test(groups = TestGroup.UNIT)
public class AbstractSecurityLoaderTest {
  private static final Map<ExternalIdBundle, UniqueId> SECURITY_IDS = new HashMap<>();
  private static final Map<UniqueId, Security> SECURITIES = new HashMap<>();
  private static final RawSecurity SEC_1 = new RawSecurity("one");
  private static final RawSecurity SEC_2 = new RawSecurity("two");
  private static final RawSecurity SEC_3 = new RawSecurity("three");
  private static final RawSecurity SEC_4 = new RawSecurity("four");
  static {
    SECURITY_IDS.put(ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "1")), UniqueId.of("uid", "1"));
    SECURITY_IDS.put(ExternalIdBundle.of(ExternalId.of("eid1", "2"), ExternalId.of("eid2", "2")), UniqueId.of("uid", "2"));
    SECURITY_IDS.put(ExternalIdBundle.of(ExternalId.of("eid1", "3"), ExternalId.of("eid2", "3")), UniqueId.of("uid", "3"));
    SECURITY_IDS.put(ExternalIdBundle.of(ExternalId.of("eid1", "4"), ExternalId.of("eid2", "4")), UniqueId.of("uid", "4"));
    SEC_1.setUniqueId(UniqueId.of("uid", "1"));
    SEC_2.setUniqueId(UniqueId.of("uid", "2"));
    SEC_3.setUniqueId(UniqueId.of("uid", "3"));
    SEC_4.setUniqueId(UniqueId.of("uid", "4"));
    SECURITIES.put(UniqueId.of("uid", "1"), SEC_1);
    SECURITIES.put(UniqueId.of("uid", "2"), SEC_2);
    SECURITIES.put(UniqueId.of("uid", "3"), SEC_3);
    SECURITIES.put(UniqueId.of("uid", "4"), SEC_4);
  }
  private static final SecurityLoader LOADER = new SecurityLoader(SECURITY_IDS, SECURITIES);

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdBundle() {
    LOADER.loadSecurity(null);
  }

  /**
   * Tests that the iterable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIterable() {
    LOADER.loadSecurities((Iterable<ExternalIdBundle>) null);
  }

  /**
   * Tests that the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRequest() {
    LOADER.loadSecurities((SecurityLoaderRequest) null);
  }

  /**
   * Tests the behaviour when no securities can be found.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSecurityFoundForIdBundle() {
    final ExternalIdBundle id = ExternalIdBundle.of("eid3", "1");
    LOADER.loadSecurity(id);
  }

  /**
   * Tests the behaviour when no securities can be found.
   */
  public void testNoSecuritiesFoundForIdBundles() {
    final List<ExternalIdBundle> ids = Arrays.asList(ExternalIdBundle.of("eid3", "1"), ExternalIdBundle.of("eid3", "2"));
    final Map<ExternalIdBundle, UniqueId> result = LOADER.loadSecurities(ids);
    assertTrue(result.isEmpty());
  }

  /**
   * Tests that only one unique id can be returned.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOneUniqueIdForSingleIdBundle() {
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(ExternalIdBundle.of("eid", "1"), SEC_1);
    securities.put(ExternalIdBundle.of("eid", "2"), SEC_2);
    final AbstractSecurityLoader loader = new AbstractSecurityLoader() {

      @Override
      protected SecurityLoaderResult doBulkLoad(final SecurityLoaderRequest request) {
        return new SecurityLoaderResult(securities, false);
      }

    };
    loader.loadSecurity(ExternalIdBundle.of("eid", "1"));
  }

  /**
   * Tests the result of a successful load of one security.
   */
  public void testLoadSecurity() {
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid1", "1")), UniqueId.of("uid", "1"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid1", "2")), UniqueId.of("uid", "2"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid1", "3")), UniqueId.of("uid", "3"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid1", "4")), UniqueId.of("uid", "4"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid2", "1")), UniqueId.of("uid", "1"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid2", "2")), UniqueId.of("uid", "2"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid2", "3")), UniqueId.of("uid", "3"));
    assertEquals(LOADER.loadSecurity(ExternalIdBundle.of("eid2", "4")), UniqueId.of("uid", "4"));
  }

  /**
   * Tests the result of a successful load of multiple securities.
   */
  public void testLoadSecuritiesIdBundles() {
    final List<ExternalIdBundle> ids = Arrays.asList(ExternalIdBundle.of("eid1", "1"), ExternalIdBundle.of("eid1", "2"), ExternalIdBundle.of("eid1", "3"),
        ExternalIdBundle.of("eid1", "4"), ExternalIdBundle.of("eid2", "1"), ExternalIdBundle.of("eid2", "2"), ExternalIdBundle.of("eid2", "3"),
        ExternalIdBundle.of("eid2", "4"));
    final Map<ExternalIdBundle, UniqueId> result = LOADER.loadSecurities(ids);
    assertEquals(result.size(), 8);
    assertEquals(result.get(ExternalIdBundle.of("eid1", "1")), UniqueId.of("uid", "1"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "2")), UniqueId.of("uid", "2"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "3")), UniqueId.of("uid", "3"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "4")), UniqueId.of("uid", "4"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "1")), UniqueId.of("uid", "1"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "2")), UniqueId.of("uid", "2"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "3")), UniqueId.of("uid", "3"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "4")), UniqueId.of("uid", "4"));
  }

  /**
   * Tests the security loader request.
   */
  public void testLoaderSecuritiesRequest() {
    assertTrue(LOADER.loadSecurities(SecurityLoaderRequest.create(Collections.<ExternalIdBundle> emptyList())).getResultMap().isEmpty());
    final List<ExternalIdBundle> ids = Arrays.asList(ExternalIdBundle.of("eid1", "1"), ExternalIdBundle.of("eid1", "2"), ExternalIdBundle.of("eid1", "3"),
        ExternalIdBundle.of("eid1", "4"), ExternalIdBundle.of("eid2", "1"), ExternalIdBundle.of("eid2", "2"), ExternalIdBundle.of("eid2", "3"),
        ExternalIdBundle.of("eid2", "4"));
    final Map<ExternalIdBundle, UniqueId> result = LOADER.loadSecurities(SecurityLoaderRequest.create(ids)).getResultMap();
    assertEquals(result.size(), 8);
    assertEquals(result.get(ExternalIdBundle.of("eid1", "1")), UniqueId.of("uid", "1"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "2")), UniqueId.of("uid", "2"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "3")), UniqueId.of("uid", "3"));
    assertEquals(result.get(ExternalIdBundle.of("eid1", "4")), UniqueId.of("uid", "4"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "1")), UniqueId.of("uid", "1"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "2")), UniqueId.of("uid", "2"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "3")), UniqueId.of("uid", "3"));
    assertEquals(result.get(ExternalIdBundle.of("eid2", "4")), UniqueId.of("uid", "4"));
  }

  /**
   *
   */
  private static class SecurityLoader extends AbstractSecurityLoader {
    private final Map<ExternalIdBundle, UniqueId> _securityIds;
    private final Map<UniqueId, Security> _securities;

    SecurityLoader(final Map<ExternalIdBundle, UniqueId> securityIds, final Map<UniqueId, Security> securities) {
      _securityIds = securityIds;
      _securities = securities;
    }

    @Override
    protected SecurityLoaderResult doBulkLoad(final SecurityLoaderRequest request) {
      final Set<ExternalIdBundle> eids = request.getExternalIdBundles();
      final Map<ExternalIdBundle, Security> securities = new HashMap<>();
      for (final ExternalIdBundle eid : eids) {
        for (final Map.Entry<ExternalIdBundle, UniqueId> entry : _securityIds.entrySet()) {
          if (entry.getKey().containsAny(eid)) {
            securities.put(eid, _securities.get(entry.getValue()));
          }
        }
      }
      return new SecurityLoaderResult(securities, true);
    }

  }

}
