/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.pool;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.mockito.Mockito;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.testng.annotations.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.opengamma.util.async.BlockingOperation;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link BoneCPHack} class.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BoneCPHackTest {

  /**
   * Creates a configuration.
   *
   * @return  the configuration
   */
  private static BoneCPConfig createConfig() {
    final BoneCPConfig config = new BoneCPConfig();
    config.setPartitionCount(1);
    config.setLazyInit(false);
    config.setMinConnectionsPerPartition(3);
    config.setMaxConnectionsPerPartition(3);
    config.setDatasourceBean(new AbstractDataSource() {

      @Override
      public Connection getConnection() throws SQLException {
        return Mockito.mock(Connection.class);
      }

      @Override
      public Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
      }

      @Override
      public Logger getParentLogger() {
        return null;
      }
    });
    config.setConnectionTimeoutInMs(Timeout.standardTimeoutMillis());
    config.setConnectionHook(new BoneCPHack(config.getConnectionHook()));
    return config;
  }

  /**
   * Tests a blocking operation.
   *
   * @throws SQLException  if there is a problem
   */
  public void testNonBlocking() throws SQLException {
    try (BoneCP bcp = new BoneCP(createConfig())) {
    BlockingOperation.off();
      try {
        try (Connection h1 = bcp.getConnection()) {
          assertNotNull(h1);
          h1.close();
        }
        try (Connection h2 = bcp.getConnection()) {
          assertNotNull(h2);
          h2.close();
        }
        try (Connection h3 = bcp.getConnection()) {
          assertNotNull(h3);
          h3.close();
        }
      } finally {
        BlockingOperation.on();
      }
    }
  }

  /**
   * Tests a non-blocking operation.
   *
   * @throws SQLException  if there is a problem
   */
  @Test(expectedExceptions = {BlockingOperation.class })
  public void testBlocking() throws SQLException {
    try (BoneCP bcp = new BoneCP(createConfig())) {
      BlockingOperation.off();
      try {
        try (Connection h1 = bcp.getConnection()) {
          assertNotNull(h1);
        }
        try (Connection h2 = bcp.getConnection()) {
          assertNotNull(h2);
        }
        try (Connection h3 = bcp.getConnection()) {
          assertNotNull(h3);
        }
        bcp.getConnection();
        fail();
      } finally {
        BlockingOperation.on();
      }
    }
  }

}
