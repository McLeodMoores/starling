/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NonVersionedRedisSecuritySourceTest extends AbstractRedisTestCase {
//  private MockJedisPool _pool;
//
//  @Override
//  @BeforeClass
//  public void launchJedisPool() {
//    final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
//    _pool = new MockJedisPool(config, "host");
//  }
//
//  @Override
//  @AfterClass
//  public void clearJedisPool() {
//    if (_pool == null) {
//      return;
//    }
//    _pool.getResource().close();
//    _pool.destroy();
//  }
//
//  @Override
//  @BeforeMethod
//  public void clearRedisDb() {
//    final Jedis jedis = _pool.getResource();
//    jedis.flushDB();
//    _pool.returnResource(jedis);
//  }
//
//  @Override
//  protected JedisPool getJedisPool() {
//    return _pool;
//  }
//
//  @Override
//  protected String getRedisPrefix() {
//    return "prefix";
//  }
//
//  public void addSimpleGetByUniqueId() {
//    final NonVersionedRedisSecuritySource source = new NonVersionedRedisSecuritySource(getJedisPool(), getRedisPrefix());
//    addSimpleSecurity(source, "1");
//    addSimpleSecurity(source, "2");
//
//    Security security = null;
//
//    security = source.get(UniqueId.of("TEST-UNQ", "1"));
//    assertNotNull(security);
//    assertEquals("1", security.getExternalIdBundle().getValue(ExternalScheme.of("TEST-EXT")));
//
//    security = source.get(UniqueId.of("TEST-UNQ", "2"));
//    assertNotNull(security);
//    assertEquals("2", security.getExternalIdBundle().getValue(ExternalScheme.of("TEST-EXT")));
//
//    security = source.get(UniqueId.of("TEST-UNQ", "3"));
//    assertNull(security);
//  }
//
//  public void addSimpleGetByExternalId() {
//    final NonVersionedRedisSecuritySource source = new NonVersionedRedisSecuritySource(getJedisPool(), getRedisPrefix());
//    addSimpleSecurity(source, "1");
//
//    Security security = null;
//
//    security = source.getSingle(ExternalIdBundle.of(ExternalId.of("TEST-EXT", "1")));
//    assertNotNull(security);
//    assertEquals(UniqueId.of("TEST-UNQ", "1", null), security.getUniqueId());
//
//    security = source.getSingle(ExternalIdBundle.of(ExternalId.of("TEST-EXT", "3")));
//    assertNull(security);
//  }
//
//  protected void addSimpleSecurity(final NonVersionedRedisSecuritySource source, final String key) {
//    final SimpleSecurity simpleSecurity = new SimpleSecurity("FAKE TYPE");
//    simpleSecurity.setUniqueId(UniqueId.of("TEST-UNQ", key));
//    simpleSecurity.addExternalId(ExternalId.of("TEST-EXT", key));
//    simpleSecurity.addAttribute("Attribute", key);
//    simpleSecurity.setName("Name - " + key);
//    source.put(simpleSecurity);
//  }

}
