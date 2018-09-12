/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fiftyonred.mock_jedis.MockJedisPool;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NonVersionedRedisPositionSourceTest extends AbstractRedisTestCase {
  private MockJedisPool _pool;

  @Override
  @BeforeClass
  public void launchJedisPool() {
    final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    _pool = new MockJedisPool(config, "host");
  }

  @Override
  @AfterClass
  public void clearJedisPool() {
    if (_pool == null) {
      return;
    }
    _pool.getResource().close();
    _pool.destroy();
  }

  @Override
  @BeforeMethod
  public void clearRedisDb() {
    final Jedis jedis = _pool.getResource();
    jedis.flushDB();
    _pool.returnResource(jedis);
  }

  @Override
  protected JedisPool getJedisPool() {
    return _pool;
  }

  @Override
  protected String getRedisPrefix() {
    return "prefix";
  }

  @Test(expectedExceptions = {DataNotFoundException.class})
  public void emptyPortfolioSearch() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    assertNull(source.getPortfolio(UniqueId.of("TEST", "NONE"), null));
  }

  @Test(expectedExceptions = {DataNotFoundException.class})
  public void emptyPositionSearch() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    assertNull(source.getPosition(UniqueId.of("TEST", "NONE")));
  }

  public void positionAddGet() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    final SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(432));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-1")));
    position.addAttribute("Att-1", "Value-1");
    position.addAttribute("Att-2", "Value-2");

    final UniqueId uniqueId = source.storePosition(position);

    final Position result = source.getPosition(uniqueId);
    assertNotNull(result);
    assertEquals(uniqueId, result.getUniqueId());
    assertEquals(position.getQuantity(), result.getQuantity());
    assertEquals(ExternalIdBundle.of(ExternalId.of("Test", "Pos-1")), position.getSecurityLink().getExternalId());
    assertEquals("Value-1", position.getAttributes().get("Att-1"));
    assertEquals("Value-2", position.getAttributes().get("Att-2"));
  }

  public void positionStoreQuantityChange() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    final SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(432));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-1")));

    final UniqueId uniqueId = source.storePosition(position);
    position.setQuantity(new BigDecimal(9999));
    position.setUniqueId(uniqueId);
    source.updatePositionQuantity(position);

    final Position result = source.getPosition(uniqueId);
    assertNotNull(result);
    assertEquals(uniqueId, result.getUniqueId());
    assertEquals(position.getQuantity(), result.getQuantity());
  }

  public void portfolioAddGet() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    final SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    final int nPositions = 100;
    for (int i = 0; i < nPositions; i++) {
      portfolio.getRootNode().addPosition(addPosition(i));
    }

    final UniqueId uniqueId = source.storePortfolio(portfolio);

    final Portfolio result = source.getPortfolio(uniqueId, null);
    assertNotNull(result);
    assertEquals("Fibble", result.getName());
    assertNotNull(result.getRootNode());
    assertTrue(result.getRootNode().getChildNodes() == null || result.getRootNode().getChildNodes().isEmpty());
    assertEquals(nPositions, result.getRootNode().getPositions().size());
  }

  public void portfolioAddGetByName() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    final SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    final int nPositions = 5;
    for (int i = 0; i < nPositions; i++) {
      portfolio.getRootNode().addPosition(addPosition(i));
    }

    final UniqueId uniqueId = source.storePortfolio(portfolio);

    final Portfolio result = source.getByName("Fibble");
    assertNotNull(result);
    assertEquals("Fibble", result.getName());
    assertEquals(uniqueId, result.getUniqueId());
  }

  protected SimplePosition addPosition(final int quantity) {
    final SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(quantity));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-" + quantity)));
    return position;
  }

  /**
   * Test how fast we can add positions to an existing portfolio.
   * When this was run on Kirk's machine (localhost) took 9.337sec at 50k positions
   * (equivalent to 0.18674ms/position).
   */
  @Test(enabled = false)
  public void largePerformanceTest() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    final SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    final UniqueId uniqueId = source.storePortfolio(portfolio);
    final Portfolio p = source.getPortfolio(uniqueId, null);

    final long start = System.nanoTime();
    final int numPositions = 50000;
    for (int i = 0; i < numPositions; i++) {
      source.addPositionToPortfolio(p, addPosition(i));
    }
    final long end = System.nanoTime();
    final double durationInSec = (end - start) / 1000000000.0;
    System.out.println("Adding " + numPositions + " took " + durationInSec + " sec");

  }

  public void portfolioNames() {
    final NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());

    final SimplePortfolio portfolio1 = new SimplePortfolio("Fibble-1");
    final UniqueId id1 = source.storePortfolio(portfolio1);
    final SimplePortfolio portfolio2 = new SimplePortfolio("Fibble-2");
    final UniqueId id2 = source.storePortfolio(portfolio2);
    final SimplePortfolio portfolio3 = new SimplePortfolio("Fibble-3");
    final UniqueId id3 = source.storePortfolio(portfolio3);

    final Map<String, UniqueId> result = source.getAllPortfolioNames();
    assertNotNull(result);
    assertEquals(3, result.size());

    assertEquals(id1, result.get(portfolio1.getName()));
    assertEquals(id2, result.get(portfolio2.getName()));
    assertEquals(id3, result.get(portfolio3.getName()));
  }

}
